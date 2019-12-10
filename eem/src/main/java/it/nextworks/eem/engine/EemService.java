package it.nextworks.eem.engine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.nextworks.eem.ConfigurationParameters;
import it.nextworks.eem.engine.messages.InternalMessage;
import it.nextworks.eem.model.*;
import it.nextworks.eem.model.enumerates.ExperimentState;
import it.nextworks.eem.repos.ExperimentExecutionRepository;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EemService{

    private static final Logger log = LoggerFactory.getLogger(EemService.class);

    @Value("${spring.rabbitmq.host}")
    private String rabbitHost;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier(ConfigurationParameters.eemQueueExchange)
    TopicExchange messageExchange;

    //Key: experimentExecutionID; Value: EEIM
    private Map<String, ExperimentExecutionInstanceManager> experimentExecutionInstances = new HashMap<>();

    @Autowired
    private EemSubscriptionService subscriptionService;

    @Autowired
    private ExperimentExecutionRepository experimentExecutionRepository;

    public synchronized String createExperimentExecutionInstance() throws FailedOperationException{
        log.info("Received request for new Experiment Execution");
        String executionId = UUID.randomUUID().toString();
        ExperimentExecution experimentExecution = new ExperimentExecution();
        experimentExecution.executionId(executionId)
            .setState(ExperimentState.INIT);
        experimentExecutionRepository.saveAndFlush(experimentExecution);
        initNewExperimentExecutionInstanceManager(executionId);
        log.info("Experiment Execution with Id {} created and stored", experimentExecution.getExecutionId());
        log.debug("{}", experimentExecution.toString());
        return executionId;
    }

    public synchronized void runExperimentExecution(ExperimentExecutionRequest request, String runType) throws FailedOperationException, NotExistingEntityException, MalformattedElementException {
        //TODO validate request
        String executionId = request.getExecutionId();
        log.info("Received request for running Experiment Execution with Id {}", executionId);
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        if(!experimentExecutionOptional.isPresent())
            throw new NotExistingEntityException(String.format("Experiment Execution with Id %s not found", executionId));
        ExperimentExecution experimentExecution = experimentExecutionOptional.get();
        if(!experimentExecution.getState().equals(ExperimentState.INIT))
            throw new FailedOperationException(String.format("Experiment Execution with Id %s is not in INIT state", executionId));
        experimentExecution.setState(ExperimentState.CONFIGURING);
        //TODO notify
    }

    public ExperimentExecution getExperimentExecution(String experimentExecutionId) throws FailedOperationException, NotExistingEntityException{
        log.info("Received request for getting Experiment Execution with Id {}", experimentExecutionId);
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(experimentExecutionId);
        if(!experimentExecutionOptional.isPresent())
            throw new NotExistingEntityException(String.format("Experiment Execution with Id %s not found", experimentExecutionId));
        log.debug("{}", experimentExecutionOptional.get().toString());
        return experimentExecutionOptional.get();
    }

    public synchronized void abortExperimentExecution(String experimentExecutionId) throws FailedOperationException, NotExistingEntityException{

    }

    public synchronized void removeExperimentExecutionRecord(String experimentExecutionId) throws FailedOperationException, NotExistingEntityException{
        //TODO add checks on state
        log.info("Received delete request for Experiment Execution with Id {}", experimentExecutionId);
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(experimentExecutionId);
        if(!experimentExecutionOptional.isPresent())
            throw new NotExistingEntityException(String.format("Experiment Execution with Id %s not found", experimentExecutionId));
        experimentExecutionRepository.delete(experimentExecutionOptional.get());
        //TODO delete EEIM
        log.info("Experiment Execution with Id {} deleted", experimentExecutionId);
    }

    public synchronized String subscribe(ExperimentExecutionSubscriptionRequest subscriptionRequest) throws FailedOperationException, NotExistingEntityException{
        String executionId = subscriptionRequest.getExecutionId();
        log.info("Received subscribe request to Experiment Execution with Id {}", executionId);
        Optional<ExperimentExecution> experimentExecution = experimentExecutionRepository.findByExecutionId(executionId);
        if(!experimentExecution.isPresent())
            throw new NotExistingEntityException(String.format("Experiment Execution with Id %s not found", executionId));
        return subscriptionService.subscribe(subscriptionRequest);
    }

    public synchronized void unsubscribe(String subscriptionId) throws FailedOperationException, NotExistingEntityException{
        log.info("Received unsubscribe request to Experiment Execution Subscription with Id {}", subscriptionId);
        subscriptionService.unsubscribe(subscriptionId);
    }

    private void initNewExperimentExecutionInstanceManager(String experimentExecutionId) {
        log.info("Initializing new Experiment Execution Instance Manager with Id {}", experimentExecutionId);
        ExperimentExecutionInstanceManager eeim = new ExperimentExecutionInstanceManager(experimentExecutionId);
        createQueue(experimentExecutionId, eeim);
        experimentExecutionInstances.put(experimentExecutionId, eeim);
        log.debug("Experiment Execution Instance Manager with Id {} initialized", experimentExecutionId);
    }

    private void createQueue(String experimentExecutionId, ExperimentExecutionInstanceManager eeim) {
        String queueName = ConfigurationParameters.eemQueueNamePrefix + experimentExecutionId;
        log.debug("Creating new Queue " + queueName + " in rabbit host " + rabbitHost);
        CachingConnectionFactory cf = new CachingConnectionFactory();
        cf.setAddresses(rabbitHost);
        cf.setConnectionTimeout(5);
        RabbitAdmin rabbitAdmin = new RabbitAdmin(cf);
        Queue queue = new Queue(queueName, false, false, true);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(messageExchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(messageExchange).with("lifecycle.*." + experimentExecutionId));
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(cf);
        MessageListenerAdapter adapter = new MessageListenerAdapter(eeim, "receiveMessage");
        container.setMessageListener(adapter);
        container.setQueueNames(queueName);
        container.start();
        log.debug("Queue created");
    }

    private void sendMessageToQueue(InternalMessage msg, String topic) throws JsonProcessingException {
        ObjectMapper mapper = buildObjectMapper();
        String json = mapper.writeValueAsString(msg);
        rabbitTemplate.convertAndSend(messageExchange.getName(), topic, json);
    }

    private ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
