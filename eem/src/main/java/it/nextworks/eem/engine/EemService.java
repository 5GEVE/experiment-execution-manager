package it.nextworks.eem.engine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.nextworks.eem.configuration.ConfigurationParameters;
import it.nextworks.eem.model.enumerate.ExperimentRunType;
import it.nextworks.eem.rabbitMessage.*;
import it.nextworks.eem.model.*;
import it.nextworks.eem.model.enumerate.ExperimentState;
import it.nextworks.eem.repo.ExperimentExecutionRepository;
import it.nextworks.eem.sbi.ConfiguratorService;
import it.nextworks.eem.sbi.ExecutorService;
import it.nextworks.eem.sbi.MultiSiteOrchestratorService;
import it.nextworks.eem.sbi.ValidatorService;
import it.nextworks.eem.sbi.expcatalogue.ExperimentCatalogueService;
import it.nextworks.eem.sbi.msno.MsnoDriver;
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

import javax.annotation.PostConstruct;
import java.util.*;

/*
TODO modificare macchina a stati EEM

- Aggiungere freccia da VALIDATING a FAILED
- Da RUNNING a PAUSED invertire verso frecce
- Eliminare freccia da PAUSED a VALIDATING ed aggiungerla da RUNNING_STEP a VALIDATING
- Aggiungere freccia da RUNNING_STEP ad ABORTING

*/

@Service
public class EemService{

    private static final Logger log = LoggerFactory.getLogger(EemService.class);

    @Value("${spring.rabbitmq.host}")
    private String rabbitHost;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier(ConfigurationParameters.eemQueueExchange)
    private TopicExchange messageExchange;

    //Key: experimentExecutionID; Value: EEIM
    private Map<String, ExperimentExecutionInstanceManager> experimentExecutionInstances = new HashMap<>();

    @Autowired
    private EemSubscriptionService subscriptionService;

    @Autowired
    private ConfiguratorService configuratorService;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private ValidatorService validatorService;

    @Autowired
    private ExperimentCatalogueService catalogueService;

    @Autowired
    private MultiSiteOrchestratorService multiSiteOrchestratorService;

    @Autowired
    private ExperimentExecutionRepository experimentExecutionRepository;

    @PostConstruct
    private void initStoredExperimentExecution() throws FailedOperationException{
        //Loads Experiment Executions stored and initializes the corresponding EEIM
        List<ExperimentExecution> experimentExecutions = experimentExecutionRepository.findAll();
        for(ExperimentExecution experimentExecution : experimentExecutions)
            if(!experimentExecution.getState().equals(ExperimentState.COMPLETED) && !experimentExecution.getState().equals(ExperimentState.ABORTED) && !experimentExecution.getState().equals(ExperimentState.FAILED)){
                initNewExperimentExecutionInstanceManager(experimentExecution.getExecutionId());
                log.info("Experiment Execution with Id {} restored in state {}", experimentExecution.getExecutionId(), experimentExecution.getState().toString());
                log.debug("{}", experimentExecution.toString());
            }
    }

    public List<ExperimentExecution> getExperimentExecutions(ExperimentState state) throws FailedOperationException{
        log.info("Received request for getting Experiment Executions list");
        if(state == null)
            return experimentExecutionRepository.findAll();
        else
            return experimentExecutionRepository.findByState(state);
    }

    public synchronized String createExperimentExecutionInstance() throws FailedOperationException{
        log.info("Received request for new Experiment Execution");
        String executionId = UUID.randomUUID().toString();//TODO use the id generated from the db?
        ExperimentExecution experimentExecution = new ExperimentExecution();
        experimentExecution.executionId(executionId)
                .state(ExperimentState.INIT);
        experimentExecutionRepository.saveAndFlush(experimentExecution);
        initNewExperimentExecutionInstanceManager(executionId);
        log.info("Experiment Execution with Id {} created and stored", experimentExecution.getExecutionId());
        log.debug("{}", experimentExecution.toString());
        return executionId;
    }

    public ExperimentExecution getExperimentExecution(String experimentExecutionId) throws FailedOperationException, NotExistingEntityException{
        log.info("Received request for getting Experiment Execution with Id {}", experimentExecutionId);
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(experimentExecutionId);
        if(!experimentExecutionOptional.isPresent())
            throw new NotExistingEntityException(String.format("Experiment Execution with Id %s not found", experimentExecutionId));
        log.debug("{}", experimentExecutionOptional.get().toString());
        return experimentExecutionOptional.get();
    }

    public synchronized void removeExperimentExecutionRecord(String experimentExecutionId) throws FailedOperationException, NotExistingEntityException{
        log.info("Received request for deleting Experiment Execution with Id {}", experimentExecutionId);
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(experimentExecutionId);
        if(!experimentExecutionOptional.isPresent())
            throw new NotExistingEntityException(String.format("Experiment Execution with Id %s not found", experimentExecutionId));
        ExperimentExecution experimentExecution = experimentExecutionOptional.get();
        if(!experimentExecution.getState().equals(ExperimentState.INIT) && !experimentExecution.getState().equals(ExperimentState.FAILED) && !experimentExecution.getState().equals(ExperimentState.ABORTED) && !experimentExecution.getState().equals(ExperimentState.COMPLETED))
            throw new FailedOperationException(String.format("Experiment Execution with Id %s is neither in INIT or FAILED or ABORTED or COMPLETED state", experimentExecutionId));
        experimentExecutionRepository.delete(experimentExecutionOptional.get());
        experimentExecutionInstances.remove(experimentExecutionId);
        subscriptionService.deleteAllSubscriptions(experimentExecutionId);
        log.info("Experiment Execution with Id {} deleted", experimentExecutionId);
    }

    public synchronized void runExperimentExecution(String executionId, ExperimentExecutionRequest request, ExperimentRunType runType) throws FailedOperationException, NotExistingEntityException, MalformattedElementException {
        request.isValid();
        log.info("Received request for running Experiment Execution with Id {}", executionId);
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        if(!experimentExecutionOptional.isPresent())
            throw new NotExistingEntityException(String.format("Experiment Execution with Id %s not found", executionId));
        ExperimentExecution experimentExecution = experimentExecutionOptional.get();
        if(!experimentExecution.getState().equals(ExperimentState.INIT))
            throw new FailedOperationException(String.format("Experiment Execution with Id %s is not in INIT state", executionId));
        //Put user parameters to be overwritten for the given run inside the experiment execution object
        List<TestCaseExecutionConfiguration> testCaseExecutionConfigurations = new ArrayList<>();
        request.getTestCaseDescriptorConfiguration().forEach((x, y) -> testCaseExecutionConfigurations.add(new TestCaseExecutionConfiguration(x, y)));
        experimentExecution.experimentDescriptorId(request.getExperimentDescriptorId())
                .nsInstanceId(request.getNsInstanceId())
                .tenantId(request.getTenantId())
                .experimentId(request.getExperimentId())
                .siteNames(request.getSiteNames())
                .testCaseDescriptorConfiguration(testCaseExecutionConfigurations)
                .useCase(request.getUseCase())
                .runType(runType);
        experimentExecutionRepository.saveAndFlush(experimentExecution);
        experimentExecutionInstances.get(executionId).setRunType(runType);

        String topic = "lifecycle.run." + executionId;
        InternalMessage internalMessage = new RunExperimentInternalMessage();;
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            throw new FailedOperationException("Internal error with queues");
        }
    }

    public synchronized void abortExperimentExecution(String experimentExecutionId) throws FailedOperationException, NotExistingEntityException{
        log.info("Received request for aborting Experiment Execution with Id {}", experimentExecutionId);
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(experimentExecutionId);
        if(!experimentExecutionOptional.isPresent())
            throw new NotExistingEntityException(String.format("Experiment Execution with Id %s not found", experimentExecutionId));
        ExperimentExecution experimentExecution = experimentExecutionOptional.get();
        if(!experimentExecution.getState().equals(ExperimentState.RUNNING) && !experimentExecution.getState().equals(ExperimentState.RUNNING_STEP) && !experimentExecution.getState().equals(ExperimentState.PAUSED) && !experimentExecution.getState().equals(ExperimentState.CONFIGURING))
            throw new FailedOperationException(String.format("Experiment Execution with Id %s is neither in RUNNING or RUNNING_STEP or PAUSED state", experimentExecutionId));

        String topic = "lifecycle.abort." + experimentExecutionId;
        InternalMessage internalMessage = new AbortExperimentInternalMessage();
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            throw new FailedOperationException("Internal error with queues");
        }
    }

    public synchronized void resumeExperimentExecution(String experimentExecutionId) throws FailedOperationException, NotExistingEntityException{
        log.info("Received request for resuming Experiment Execution with Id {}", experimentExecutionId);
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(experimentExecutionId);
        if(!experimentExecutionOptional.isPresent())
            throw new NotExistingEntityException(String.format("Experiment Execution with Id %s not found", experimentExecutionId));
        ExperimentExecution experimentExecution = experimentExecutionOptional.get();
        if(!experimentExecution.getRunType().equals(ExperimentRunType.RUN_ALL))
            throw new FailedOperationException(String.format("Experiment Execution with Id %s is not a RUN_ALL execution", experimentExecutionId));
        if(!experimentExecution.getState().equals(ExperimentState.PAUSED))
            throw new FailedOperationException(String.format("Experiment Execution with Id %s is not in PAUSED state", experimentExecutionId));

        String topic = "lifecycle.resume." + experimentExecutionId;
        InternalMessage internalMessage = new ResumeExperimentInternalMessage();
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            throw new FailedOperationException("Internal error with queues");
        }
    }

    public synchronized void pauseExperimentExecution(String experimentExecutionId) throws FailedOperationException, NotExistingEntityException{
        log.info("Received request for pausing Experiment Execution with Id {}", experimentExecutionId);
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(experimentExecutionId);
        if(!experimentExecutionOptional.isPresent())
            throw new NotExistingEntityException(String.format("Experiment Execution with Id %s not found", experimentExecutionId));
        ExperimentExecution experimentExecution = experimentExecutionOptional.get();
        if(!experimentExecution.getState().equals(ExperimentState.RUNNING))
            throw new FailedOperationException(String.format("Experiment Execution with Id %s is not in RUNNING state", experimentExecutionId));

        String topic = "lifecycle.pause." + experimentExecutionId;
        InternalMessage internalMessage = new PauseExperimentInternalMessage();
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            throw new FailedOperationException("Internal error with queues");
        }
    }

    public synchronized void stepExperimentExecution(String experimentExecutionId) throws FailedOperationException, NotExistingEntityException{
        log.info("Received request for performing a step of the Experiment Execution with Id {}", experimentExecutionId);
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(experimentExecutionId);
        if(!experimentExecutionOptional.isPresent())
            throw new NotExistingEntityException(String.format("Experiment Execution with Id %s not found", experimentExecutionId));
        ExperimentExecution experimentExecution = experimentExecutionOptional.get();
        if(!experimentExecution.getState().equals(ExperimentState.PAUSED))
            throw new FailedOperationException(String.format("Experiment Execution with Id %s is not in PAUSED state", experimentExecutionId));

        String topic = "lifecycle.step." + experimentExecutionId;
        InternalMessage internalMessage = new StepExperimentInternalMessage();
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            throw new FailedOperationException("Internal error with queues");
        }
    }

    public List<ExperimentExecutionSubscription> getExperimentExecutionSubscriptions() throws FailedOperationException{
        log.info("Received request for getting Experiment Execution Subscription list");
        return subscriptionService.getExperimentExecutionSubscriptions();
    }

    public ExperimentExecutionSubscription getExperimentExecutionSubscription(String subscriptionId) throws FailedOperationException, NotExistingEntityException{
        log.info("Received request for getting Experiment Execution Subscription with Id {}", subscriptionId);
        return subscriptionService.getExperimentExecutionSubscription(subscriptionId);
    }

    public synchronized String subscribe(ExperimentExecutionSubscriptionRequest subscriptionRequest) throws FailedOperationException, NotExistingEntityException, MalformattedElementException{
        subscriptionRequest.isValid();
        String executionId = subscriptionRequest.getExecutionId();
        if(executionId.equals("*")){
            log.info("Received subscribe request to all Experiment Executions");
        }else {
            log.info("Received subscribe request to Experiment Execution with Id {}", executionId);
            Optional<ExperimentExecution> experimentExecution = experimentExecutionRepository.findByExecutionId(executionId);
            if (!experimentExecution.isPresent())
                throw new NotExistingEntityException(String.format("Experiment Execution with Id %s not found", executionId));
        }
        return subscriptionService.subscribe(subscriptionRequest);
    }

    public synchronized void unsubscribe(String subscriptionId) throws FailedOperationException, NotExistingEntityException{
        log.info("Received unsubscribe request to Experiment Execution Subscription with Id {}", subscriptionId);
        subscriptionService.unsubscribe(subscriptionId);
    }

    private void initNewExperimentExecutionInstanceManager(String experimentExecutionId) throws FailedOperationException{
        log.info("Initializing new Experiment Execution Instance Manager with Id {}", experimentExecutionId);
        ExperimentExecutionInstanceManager eeim;
        try {
            eeim = new ExperimentExecutionInstanceManager(experimentExecutionId, experimentExecutionRepository, subscriptionService, configuratorService, executorService, validatorService, catalogueService, multiSiteOrchestratorService);
        }catch (NotExistingEntityException e) {
            throw new FailedOperationException(String.format("Initialization of Experiment Execution Instance Manager with Id %s failed : %s", experimentExecutionId, e.getMessage()));
        }
        createQueue(experimentExecutionId, eeim);
        experimentExecutionInstances.put(experimentExecutionId, eeim);
        log.debug("Experiment Execution Instance Manager with Id {} initialized", experimentExecutionId);
    }

    private void createQueue(String experimentExecutionId, ExperimentExecutionInstanceManager eeim) {
        String queueName = ConfigurationParameters.eemQueueInNamePrefix + experimentExecutionId;
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
