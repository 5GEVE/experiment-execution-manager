package it.nextworks.eem.engine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.nextworks.eem.configuration.ConfigurationParameters;
import it.nextworks.eem.model.ExecutionResult;
import it.nextworks.eem.rabbitMessage.*;
import it.nextworks.eem.model.ExperimentExecution;
import it.nextworks.eem.model.ExperimentExecutionStateChangeNotification;
import it.nextworks.eem.model.enumerate.ExperimentState;
import it.nextworks.eem.repo.ExperimentExecutionRepository;
import it.nextworks.eem.sbi.jenkins.SbiJenkinsService;
import it.nextworks.nfvmano.catalogue.blueprint.elements.*;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException;
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
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.*;

//TODO persist??
public class ExperimentExecutionInstanceManager {

    private static final Logger log = LoggerFactory.getLogger(ExperimentExecutionInstanceManager.class);

    private String executionId;
    private ExperimentState state;

    private RabbitTemplate rabbitTemplate;
    private TopicExchange messageExchange;
    private String rabbitHost;

    private SbiJenkinsService jenkinsService;
    private EemSubscriptionService subscriptionService;
    private ExperimentExecutionRepository experimentExecutionRepository;

    private boolean interruptRunning;

    private ExpDescriptor expDescriptor;
    private VsDescriptor vsDescriptor;
    private List<CtxDescriptor> ctxDescriptors = new ArrayList<>();
    private List<TestCaseDescriptor> tcDescriptors = new ArrayList<>();
    private List<TestCaseBlueprint> tcBlueprints = new ArrayList<>();

    //Key: tcDescriptorId, Value: robotFile
    private Map<String, String> testCases = new LinkedHashMap<>();//TODO change value type in RobotFile format, change also inside RUN_TEST_CASE message
    private Iterator<Map.Entry<String, String>> testCasesIterator;

    public ExperimentExecutionInstanceManager(String executionId, ExperimentExecutionRepository experimentExecutionRepository, EemSubscriptionService subscriptionService, RabbitTemplate rabbitTemplate, TopicExchange messageExchange, String rabbitHost){
        this.executionId = executionId;
        this.state = ExperimentState.INIT;
        this.experimentExecutionRepository = experimentExecutionRepository;
        this.subscriptionService = subscriptionService;
        this.interruptRunning = false;
        this.rabbitTemplate = rabbitTemplate;
        this.messageExchange = messageExchange;
        this.rabbitHost = rabbitHost;
        initNewJenkinsService();
    }

    /**
     * Method used to receive messages about experiment execution LCM from the Rabbit MQ
     *
     * @param message received message
     */
    public void receiveMessage(String message) {
        log.info("Received message for Experiment Execution {}", executionId);
        log.debug(message);
        try {
            ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
                    .modules(new JavaTimeModule())
                    .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                    .build();
            InternalMessage im = mapper.readValue(message, InternalMessage.class);
            InternalMessageType imt = im.getType();

            switch (imt) {
                case RUN_ALL: {
                    log.debug("Processing request to run all the Experiment Execution with Id {}", executionId);
                    RunAllExperimentInternalMessage msg = (RunAllExperimentInternalMessage) im;
                    processRunAllRequest(msg);
                    break;
                }
                case RUN_STEP: {
                    log.debug("Processing request to run step by step the Experiment Execution with Id {}", executionId);
                    RunStepExperimentInternalMessage msg = (RunStepExperimentInternalMessage) im;
                    processRunStepRequest(msg);
                    break;
                }
                case PAUSE: {
                    log.debug("Processing request to pause the experiment execution with Id {}", executionId);
                    PauseExperimentInternalMessage msg = (PauseExperimentInternalMessage) im;
                    processPauseRequest(msg);
                    break;
                }
                case RESUME: {
                    log.debug("Processing request to resume the Experiment Execution with Id {}", executionId);
                    ResumeExperimentInternalMessage msg = (ResumeExperimentInternalMessage) im;
                    processResumeRequest(msg);
                    break;
                }
                case STEP: {
                    log.debug("Processing request to run a Test Case of the Experiment Execution with Id {}", executionId);
                    StepExperimentInternalMessage msg = (StepExperimentInternalMessage) im;
                    processStepRequest(msg);
                    break;
                }
                case ABORT: {
                    log.debug("Processing request to abort the Experiment Execution with Id {}", executionId);
                    AbortExperimentInternalMessage msg = (AbortExperimentInternalMessage) im;
                    processAbortRequest(msg);
                    break;
                }
                case RESULT: {
                    TestCaseResultInternalMessage msg = (TestCaseResultInternalMessage) im;
                    log.debug("Processing result of Test Case with Id {} for the Experiment Execution with Id {}", msg.getTcDescriptorId(), executionId);
                    processTestCaseResult(msg);
                    break;
                }
                default:
                    log.error("Received message with not supported type. Skipping.");
                    break;
            }
        } catch (JsonParseException e) {
            log.debug(null, e);
            manageExperimentExecutionError("Error while parsing message: " + e.getMessage());
        } catch (JsonMappingException e) {
            log.debug(null, e);
            manageExperimentExecutionError("Error in Json mapping: " + e.getMessage());
        } catch (IOException e) {
            log.debug(null, e);
            manageExperimentExecutionError("IO error when receiving json message: " + e.getMessage());
        }
    }

    private void processRunAllRequest(RunAllExperimentInternalMessage msg){
        try {
            configureExperimentExecution();
            updateAndNotifyExperimentExecutionState(ExperimentState.RUNNING);
            log.info("Running Experiment Execution with Id {}", executionId);
            runExperimentExecutionTestCase();
        }catch (FailedOperationException e){
            log.debug(null, e);
            manageExperimentExecutionError("Error while configuring the Experiment Execution" + e.getMessage());
        }
    }

    private void processRunStepRequest(RunStepExperimentInternalMessage msg){
        try {
            configureExperimentExecution();
            updateAndNotifyExperimentExecutionState(ExperimentState.PAUSED);
            log.info("Experiment Execution with Id {} paused", executionId);
        }catch (FailedOperationException e){
            log.debug(null, e);
            manageExperimentExecutionError("Error while configuring the Experiment Execution" + e.getMessage());
        }
    }

    private void processPauseRequest(PauseExperimentInternalMessage msg){
        log.info("Pausing Experiment Execution with Id {}", executionId);
        interruptRunning = true;
    }

    private void processResumeRequest(ResumeExperimentInternalMessage msg){
        log.info("Resuming Experiment Execution with Id {}", executionId);
        updateAndNotifyExperimentExecutionState(ExperimentState.RUNNING);
        runExperimentExecutionTestCase();
        log.info("Experiment Execution with Id {} resumed", executionId);
    }

    private void processStepRequest(StepExperimentInternalMessage msg){
        log.info("Running a step of Experiment Execution with Id {}", executionId);
        updateAndNotifyExperimentExecutionState(ExperimentState.RUNNING_STEP);
        runExperimentExecutionTestCase();
    }

    private void processAbortRequest(AbortExperimentInternalMessage msg){
        log.info("Aborting Experiment Execution with Id {}", executionId);
        updateAndNotifyExperimentExecutionState(ExperimentState.ABORTING);
    }

    private void processTestCaseResult(TestCaseResultInternalMessage msg){
        String testCaseId = msg.getTcDescriptorId();
        log.info("Processing result of Test Case with Id {} of Experiment Execution with Id {}", testCaseId, executionId);
        if(msg.isFailed()) {
            manageExperimentExecutionError(msg.getResult());//TODO if a test case fails, all the experiment execution fails?
            return;
        }
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        ExperimentExecution experimentExecution = experimentExecutionOptional.get();
        ExecutionResult executionResult = new ExecutionResult();
        executionResult.result(msg.getResult());//TODO reportUrl?
        experimentExecution.addTestCaseResult(testCaseId, executionResult);
        experimentExecutionRepository.saveAndFlush(experimentExecution);
        log.info("Experiment Execution Test Case with Id {} completed", testCaseId);
        testCasesIterator.remove();
        if(testCases.size() == 0){
            try {
                validateExperimentExecution();
                log.info("Experiment Execution with Id {} completed", executionId);
                updateAndNotifyExperimentExecutionState(ExperimentState.COMPLETED);
            }catch (FailedOperationException e){
                log.debug(null, e);
                manageExperimentExecutionError("Error while validating Experiment Execution");
            }
        }else if(state.equals(ExperimentState.RUNNING_STEP) || (state.equals(ExperimentState.RUNNING) && interruptRunning)) {
            updateAndNotifyExperimentExecutionState(ExperimentState.PAUSED);
            interruptRunning = false;
            log.info("Experiment Execution with Id {} paused", executionId);
        }else if(state.equals(ExperimentState.ABORTING)){
            //TODO abort experiment execution
            updateAndNotifyExperimentExecutionState(ExperimentState.ABORTED);
            log.info("Experiment Execution with Id {} aborted", executionId);
        }
    }

    private void runExperimentExecutionTestCase(){
        if(testCasesIterator.hasNext()) {
            Map.Entry<String, String> testCaseToRun = testCasesIterator.next();
            String tcDescriptorId = testCaseToRun.getKey();
            log.info("Running Experiment Execution Test Case with Id {}", tcDescriptorId);
            String topic = String.format("testCase.%s.%s", tcDescriptorId, executionId);
            InternalMessage internalMessage = new RunTestCaseInternalMessage(tcDescriptorId, testCaseToRun.getValue());
            try {
                sendMessageToQueue(internalMessage, topic);
            } catch (JsonProcessingException e) {
                log.error("Error while translating internal scheduling message in Json format");
                manageExperimentExecutionError("Internal error with queues");
            }
        }else
            log.debug("No more Test Cases to run");
    }

    private void configureExperimentExecution() throws FailedOperationException {
        log.info("Configuring Experiment Execution with Id {}", executionId);
        updateAndNotifyExperimentExecutionState(ExperimentState.CONFIGURING);
        //TODO configure experiment execution and handle error condition
        testCasesIterator = testCases.entrySet().iterator();
        log.info("Configuration of Experiment Execution with Id {} completed", executionId);
    }

    private void validateExperimentExecution() throws FailedOperationException {
        log.info("Validating Experiment Execution with Id {}", executionId);
        updateAndNotifyExperimentExecutionState(ExperimentState.VALIDATING);
        //TODO validate experiment execution and handle error conditions
        log.info("Experiment Execution with Id {} validated", executionId);
    }

    private void updateAndNotifyExperimentExecutionState(ExperimentState newState){
        ExperimentState previousState = this.state;
        this.state = newState;
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        experimentExecutionOptional.ifPresent(experimentExecution -> experimentExecutionRepository.saveAndFlush(experimentExecution.state(state)));
        ExperimentExecutionStateChangeNotification msg = new ExperimentExecutionStateChangeNotification(executionId, state);
        subscriptionService.notifyExperimentExecutionStateChange(msg, previousState);
        //TODO handle notification with queue?
    }

    private void manageExperimentExecutionError(String errorMessage){
        log.error("Exeperiment Execution with Id {} failed : {}", executionId, errorMessage);
        updateAndNotifyExperimentExecutionState(ExperimentState.FAILED);
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        experimentExecutionOptional.ifPresent(experimentExecution -> experimentExecutionRepository.saveAndFlush(experimentExecution.errorMessage(errorMessage)));
    }

    private void initNewJenkinsService() {
        log.info("Initializing new Jenkins Service with Id {}", executionId);
        jenkinsService = new SbiJenkinsService(executionId, rabbitTemplate, messageExchange);
        createQueue();
        log.debug("Jenkins Service with Id {} initialized", executionId);
    }

    private void createQueue() {
        String queueName = ConfigurationParameters.eemQueueOutNamePrefix + executionId;
        log.debug("Creating new Queue " + queueName + " in rabbit host " + rabbitHost);
        CachingConnectionFactory cf = new CachingConnectionFactory();
        cf.setAddresses(rabbitHost);
        cf.setConnectionTimeout(5);
        RabbitAdmin rabbitAdmin = new RabbitAdmin(cf);
        org.springframework.amqp.core.Queue queue = new Queue(queueName, false, false, true);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(messageExchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(messageExchange).with("testCase.*." + executionId));
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(cf);
        MessageListenerAdapter adapter = new MessageListenerAdapter(jenkinsService, "receiveMessage");
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
