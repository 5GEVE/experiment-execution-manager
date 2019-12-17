package it.nextworks.eem.engine;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.nextworks.eem.model.ExecutionResult;
import it.nextworks.eem.model.TestCaseExecutionConfiguration;
import it.nextworks.eem.rabbitMessage.*;
import it.nextworks.eem.model.ExperimentExecution;
import it.nextworks.eem.model.ExperimentExecutionStateChangeNotification;
import it.nextworks.eem.model.enumerate.ExperimentState;
import it.nextworks.eem.repo.ExperimentExecutionRepository;
import it.nextworks.eem.sbi.expcatalogue.ExperimentCatalogueService;
import it.nextworks.eem.sbi.jenkins.JenkinsService;
import it.nextworks.eem.sbi.msno.MsnoService;
import it.nextworks.eem.sbi.runtimeConfigurator.ConfigurationService;
import it.nextworks.eem.sbi.validationComponent.ValidationService;
import it.nextworks.nfvmano.catalogue.blueprint.elements.*;
import it.nextworks.nfvmano.libs.ifa.common.elements.Filter;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.common.messages.GeneralizedQueryRequest;
import it.nextworks.openapi.msno.model.NsInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ExperimentExecutionInstanceManager {

    private static final Logger log = LoggerFactory.getLogger(ExperimentExecutionInstanceManager.class);

    private String executionId;
    private ExperimentState currentState;

    private JenkinsService jenkinsService;
    private ValidationService validationService;
    private EemSubscriptionService subscriptionService;
    private ConfigurationService configurationService;
    private ExperimentCatalogueService catalogueService;
    private MsnoService msnoService;
    private ExperimentExecutionRepository experimentExecutionRepository;

    private boolean interruptRunning;

    private ExpDescriptor expDescriptor;
    private VsDescriptor vsDescriptor;
    private List<CtxDescriptor> ctxDescriptors = new ArrayList<>();
    private List<TestCaseDescriptor> tcDescriptors = new ArrayList<>();
    private List<TestCaseBlueprint> tcBlueprints = new ArrayList<>();
    private NsInstance nsInstance;

    //Key: tcDescriptorId, Value: robotFile
    private Map<String, String> testCases = new LinkedHashMap<>();//TODO change value type in RobotFile format, change also inside RUN_TEST_CASE message
    private Iterator<Map.Entry<String, String>> testCasesIterator;

    public ExperimentExecutionInstanceManager(String executionId, ExperimentExecutionRepository experimentExecutionRepository, EemSubscriptionService subscriptionService, JenkinsService jenkinsService, ValidationService validationService, ConfigurationService configurationService, ExperimentCatalogueService catalogueService, MsnoService msnoService) throws NotExistingEntityException
    {
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        if(!experimentExecutionOptional.isPresent())
            throw new NotExistingEntityException(String.format("Experiment Execution with Id %s not found", executionId));
        this.executionId = executionId;
        this.currentState = experimentExecutionOptional.get().getState();
        this.experimentExecutionRepository = experimentExecutionRepository;
        this.subscriptionService = subscriptionService;
        this.interruptRunning = false;
        this.jenkinsService = jenkinsService;
        this.validationService = validationService;
        this.configurationService = configurationService;
        this.catalogueService = catalogueService;
        this.msnoService = msnoService;
        if(!this.currentState.equals(ExperimentState.INIT))
            retrieveAllInformation();
        switch(currentState){
            case CONFIGURING:
                configurationService.configureExperiment(executionId, "RUN_ALL");//TODO distinguish between RUN_ALL and RUN_IN_STEPS
                break;
            case RUNNING: case RUNNING_STEP:
                runExperimentExecutionTestCase();
                break;
            case VALIDATING:
                validationService.validateExperiment(executionId);
                break;
            case ABORTING:
                abortExeperimentExecution();
                break;
            default:
                log.debug("There aren't pending operations for Experiment Execution with Id {}", executionId);
        }
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
                    log.debug("Processing request to run all Experiment Execution with Id {}", executionId);
                    RunAllExperimentInternalMessage msg = (RunAllExperimentInternalMessage) im;
                    processRunAllRequest(msg);
                    break;
                }
                case RUN_STEP: {
                    log.debug("Processing request to run step by step Experiment Execution with Id {}", executionId);
                    RunStepExperimentInternalMessage msg = (RunStepExperimentInternalMessage) im;
                    processRunStepRequest(msg);
                    break;
                }
                case PAUSE: {
                    log.debug("Processing request to pause Experiment Execution with Id {}", executionId);
                    PauseExperimentInternalMessage msg = (PauseExperimentInternalMessage) im;
                    processPauseRequest(msg);
                    break;
                }
                case RESUME: {
                    log.debug("Processing request to resume Experiment Execution with Id {}", executionId);
                    ResumeExperimentInternalMessage msg = (ResumeExperimentInternalMessage) im;
                    processResumeRequest(msg);
                    break;
                }
                case STEP: {
                    log.debug("Processing request to run a Test Case of Experiment Execution with Id {}", executionId);
                    StepExperimentInternalMessage msg = (StepExperimentInternalMessage) im;
                    processStepRequest(msg);
                    break;
                }
                case ABORT: {
                    log.debug("Processing request to abort Experiment Execution with Id {}", executionId);
                    AbortExperimentInternalMessage msg = (AbortExperimentInternalMessage) im;
                    processAbortRequest(msg);
                    break;
                }
                case TC_RESULT: {
                    TestCaseResultInternalMessage msg = (TestCaseResultInternalMessage) im;
                    log.debug("Processing result of Test Case with Id {} of Experiment Execution with Id {}", msg.getTcDescriptorId(), executionId);
                    processTestCaseResult(msg);
                    break;
                }case VALIDATION_RESULT: {
                    ValidationResultInternalMessage msg = (ValidationResultInternalMessage) im;
                    log.debug("Processing validation result of Experiment Execution with Id {}", executionId);
                    processValidationResult(msg);
                    break;
                }case CONFIGURATION_RESULT: {
                    ConfigurationResultInternalMessage msg = (ConfigurationResultInternalMessage) im;
                    log.debug("Processing configuration result of Experiment Execution with Id {}", executionId);
                    processConfigurationResult(msg);
                    break;
                }
                default:
                    log.error("Received message with not supported type. Skipping");
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
        } catch (Exception e){
            log.debug("Unhandled Exception");
        }
    }

    private void processRunAllRequest(RunAllExperimentInternalMessage msg){
        if(updateAndNotifyExperimentExecutionState(ExperimentState.CONFIGURING))
            log.info("Configuring Experiment Execution with Id {}", executionId);
        retrieveAllInformation();
        configurationService.configureExperiment(executionId, "RUN_ALL");
    }

    private void processRunStepRequest(RunStepExperimentInternalMessage msg){
        if(updateAndNotifyExperimentExecutionState(ExperimentState.CONFIGURING))
            log.info("Configuring Experiment Execution with Id {}", executionId);
        retrieveAllInformation();
        configurationService.configureExperiment(executionId, "RUN_IN_STEPS");
    }

    private void processPauseRequest(PauseExperimentInternalMessage msg){
        log.info("Pausing Experiment Execution with Id {}", executionId);
        interruptRunning = true;
    }

    private void processResumeRequest(ResumeExperimentInternalMessage msg){
        if(updateAndNotifyExperimentExecutionState(ExperimentState.RUNNING)) {
            log.info("Resuming Experiment Execution with Id {}", executionId);
            runExperimentExecutionTestCase();
            log.info("Experiment Execution with Id {} resumed", executionId);
        }
    }

    private void processStepRequest(StepExperimentInternalMessage msg){
        if(updateAndNotifyExperimentExecutionState(ExperimentState.RUNNING_STEP)) {
            log.info("Running a step of Experiment Execution with Id {}", executionId);
            runExperimentExecutionTestCase();
        }
    }

    private void processAbortRequest(AbortExperimentInternalMessage msg){
        boolean abortNow = currentState.equals(ExperimentState.PAUSED);
        if(updateAndNotifyExperimentExecutionState(ExperimentState.ABORTING))
            log.info("Aborting Experiment Execution with Id {}", executionId);
        if(abortNow)
            abortExeperimentExecution();
    }

    private void processTestCaseResult(TestCaseResultInternalMessage msg){
        String testCaseId = msg.getTcDescriptorId();
        log.info("Processing result of Test Case with Id {} of Experiment Execution with Id {}", testCaseId, executionId);
        /*
        if(msg.isFailed()) {
            manageExperimentExecutionError(msg.getResult());
            return;
        }
         */
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        ExperimentExecution experimentExecution = experimentExecutionOptional.get();
        ExecutionResult executionResult = new ExecutionResult();
        executionResult.result(msg.getResult());//TODO reportUrl?
        experimentExecution.addTestCaseResult(testCaseId, executionResult);
        experimentExecutionRepository.saveAndFlush(experimentExecution);
        log.info("Experiment Execution Test Case with Id {} completed", testCaseId);
        testCasesIterator.remove();
        if(currentState.equals(ExperimentState.ABORTING)) {
            abortExeperimentExecution();
            return;
        }
        if(testCases.size() == 0){
            if(updateAndNotifyExperimentExecutionState(ExperimentState.VALIDATING)) {
                log.info("Validating Experiment Execution with Id {}", executionId);
                validationService.validateExperiment(executionId);
            }
        }else if(currentState.equals(ExperimentState.RUNNING_STEP) || (currentState.equals(ExperimentState.RUNNING) && interruptRunning)) {
            if(updateAndNotifyExperimentExecutionState(ExperimentState.PAUSED)) {
                interruptRunning = false;
                log.info("Experiment Execution with Id {} paused", executionId);
            }
        }else if(currentState.equals(ExperimentState.RUNNING)){
            runExperimentExecutionTestCase();
        }else {
            log.debug("State of the Execution Experiment is not correct");
        }
    }

    private void processValidationResult(ValidationResultInternalMessage msg){
        if(msg.isFailed()) {
            manageExperimentExecutionError(msg.getResult());
            return;
        }
        log.info("Experiment Execution with Id {} validated", executionId);
        //TODO get result and set something?
        if(updateAndNotifyExperimentExecutionState(ExperimentState.COMPLETED))
            log.info("Experiment Execution with Id {} completed", executionId);
    }

    private void processConfigurationResult(ConfigurationResultInternalMessage msg){
        if(msg.isFailed()) {
            manageExperimentExecutionError(msg.getResult());
            return;
        }
        if(msg.getRunType().equals("RUN_ALL")){
            if(updateAndNotifyExperimentExecutionState(ExperimentState.RUNNING)) {
                log.info("Running Experiment Execution with Id {}", executionId);
                runExperimentExecutionTestCase();
            }
        }else {
            if(updateAndNotifyExperimentExecutionState(ExperimentState.PAUSED))
                log.info("Experiment Execution with Id {} paused", executionId);
        }
        log.info("Configuration of Experiment Execution with Id {} completed", executionId);
    }

    private void runExperimentExecutionTestCase(){
        if(testCasesIterator.hasNext()) {
            Map.Entry<String, String> testCaseToRun = testCasesIterator.next();
            String tcDescriptorId = testCaseToRun.getKey();
            log.info("Running Experiment Execution Test Case with Id {}", tcDescriptorId);
            jenkinsService.runTestCase(executionId, tcDescriptorId, testCaseToRun.getValue());
        }else
            log.debug("No more Test Cases to run");
    }

    private void retrieveAllInformation(){
        log.info("Retrieving all the information for Experiment Execution with Id {}", executionId);
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        ExperimentExecution experimentExecution = experimentExecutionOptional.get();
        String experimentDescriptorId = experimentExecution.getExperimentDescriptorId();
        String nsInstanceId = experimentExecution.getNsInstanceId();
        try{
            Map<String, String> parameters = new HashMap<>();
            Filter filter = new Filter(parameters);
            GeneralizedQueryRequest request = new GeneralizedQueryRequest(filter, null);
            log.debug("Going to retrieve Experiment Descriptor with Id {}", experimentDescriptorId);
            parameters.put("ExpD_ID", experimentDescriptorId);
            expDescriptor = catalogueService.queryExpDescriptor(request).getExpDescriptors().get(0);
            parameters.remove("ExpD_ID");
            String vsDescriptorId = expDescriptor.getVsDescriptorId();
            log.debug("Going to retrieve Vertical Service Descriptor with Id {}", vsDescriptorId);
            parameters.put("VSD_ID", vsDescriptorId);
            vsDescriptor = catalogueService.queryVsDescriptor(request).getVsDescriptors().get(0);
            parameters.remove("VSD_ID");
            List<String> ctxDescriptorIds = expDescriptor.getCtxDescriptorIds();
            log.debug("Going to retrieve Context Descriptors with Ids {}", ctxDescriptorIds);
            for(String ctxDescriptorId : ctxDescriptorIds) {
                parameters.put("CTXD_ID", ctxDescriptorId);
                CtxDescriptor ctxDescriptor = catalogueService.queryCtxDescriptor(request).getCtxDescriptors().get(0);
                ctxDescriptors.add(ctxDescriptor);
                parameters.remove("CTXD_ID");
            }
            List<String> tcDescriptorIds = expDescriptor.getTestCaseDescriptorIds();
            log.debug("Going to retrieve Test Case Descriptors with Ids {}", tcDescriptorIds);
            for(String tcDescriptorId : tcDescriptorIds) {
                parameters.put("TCD_ID", tcDescriptorId);
                TestCaseDescriptor tcDescriptor = catalogueService.queryTestCaseDescriptor(request).getTestCaseDescriptors().get(0);
                tcDescriptors.add(tcDescriptor);
                parameters.remove("TCD_ID");
            }
            List<String> tcBlueprintIds = tcDescriptors.stream().map(TestCaseDescriptor::getTestCaseBlueprintId).collect(Collectors.toList());
            log.debug("Going to retrieve Test Case Blueprints with Ids {}", tcBlueprintIds);
            for(String tcBlueprintId : tcBlueprintIds) {
                parameters.put("TCB_ID", tcBlueprintId);
                TestCaseBlueprint tcBlueprint = catalogueService.queryTestCaseBlueprint(request).getTestCaseBlueprints().get(0).getTestCaseBlueprint();
                tcBlueprints.add(tcBlueprint);
                parameters.remove("TCB_ID");
            }
            log.debug("Going to retrieve NsInstance with Id {}", nsInstanceId);
            parameters.put("NS_ID", nsInstanceId);
            nsInstance = msnoService.queryNs(request);
            parameters.remove("NS_ID");
        }catch (FailedOperationException | MalformattedElementException e){
            manageExperimentExecutionError(e.getMessage());
        }
        translateTestCases();
    }

    private void translateTestCases(){
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        ExperimentExecution experimentExecution = experimentExecutionOptional.get();
        List<TestCaseExecutionConfiguration> executionConfigurations = experimentExecution.getTestCaseDescriptorConfiguration();
        tcDescriptors.forEach(x -> log.debug("testcaseDescriptor {} \n", x.toString()));//TODO remove
        for(TestCaseExecutionConfiguration executionConfiguration : executionConfigurations)
            for(TestCaseDescriptor tcDescriptor : tcDescriptors)
                if(tcDescriptor.getTestCaseDescriptorId().equals(executionConfiguration.getTcDescriptorId())) {
                    log.debug("Replacing userParameters {} with executionConfigurations {} for Test Case with Id {}", tcDescriptor.getUserParameters(), executionConfiguration.getExecConfiguration(), tcDescriptor.getTestCaseDescriptorId());
                    executionConfiguration.getExecConfiguration().forEach((x, y) -> tcDescriptor.getUserParameters().replace(x, y));
                }
        tcDescriptors.forEach(x -> log.debug("testcaseDescriptor {} \n", x.toString()));//TODO remove
        //TODO create robot files
        Set<String> executionResultIds = experimentExecution.getTestCaseResult().keySet();//TODO put only test cases not present inside testCaseResult of experiment Execution (needed for resuming the experiment)
        //TODO remove
        if(!executionResultIds.contains("testCase1"))
            testCases.put("testCase1", "test");
        if(!executionResultIds.contains("testCase2"))
            testCases.put("testCase2", "test");
        if(!executionResultIds.contains("testCase3"))
            testCases.put("testCase3", "test");

        testCasesIterator = testCases.entrySet().iterator();
    }

    private void manageExperimentExecutionError(String errorMessage){
        log.error("Exeperiment Execution with Id {} failed : {}", executionId, errorMessage);
        if(updateAndNotifyExperimentExecutionState(ExperimentState.FAILED)) {
            Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
            experimentExecutionOptional.ifPresent(experimentExecution -> experimentExecutionRepository.saveAndFlush(experimentExecution.errorMessage(errorMessage)));
        }
    }

    private void abortExeperimentExecution(){
        //TODO abort experiment execution
        if (updateAndNotifyExperimentExecutionState(ExperimentState.ABORTED))
            log.info("Experiment Execution with Id {} aborted", executionId);
    }

    private boolean updateAndNotifyExperimentExecutionState(ExperimentState newState){
        ExperimentState previousState = this.currentState;
        this.currentState = newState;
        if(!isStateChangeAllowed(previousState, newState)) {
            log.info("State change from {} to {} not allowed. Skipping", previousState, newState);
            return false;
        }
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        experimentExecutionOptional.ifPresent(experimentExecution -> experimentExecutionRepository.saveAndFlush(experimentExecution.state(currentState)));
        ExperimentExecutionStateChangeNotification msg = new ExperimentExecutionStateChangeNotification(executionId, currentState);
        subscriptionService.notifyExperimentExecutionStateChange(msg, previousState);
        return true;
    }

    private boolean isStateChangeAllowed(ExperimentState currentState, ExperimentState newState){
        switch (newState){
            case FAILED:
                return currentState.equals(ExperimentState.CONFIGURING) ||
                        currentState.equals(ExperimentState.RUNNING) ||
                        currentState.equals(ExperimentState.RUNNING_STEP) ||
                        currentState.equals(ExperimentState.VALIDATING);
            case PAUSED:
                return currentState.equals(ExperimentState.CONFIGURING) ||
                        currentState.equals(ExperimentState.RUNNING) ||
                        currentState.equals(ExperimentState.RUNNING_STEP);
            case ABORTED:
                return currentState.equals(ExperimentState.ABORTING);
            case RUNNING:
                return currentState.equals(ExperimentState.CONFIGURING) ||
                        currentState.equals(ExperimentState.PAUSED);
            case ABORTING:
                return currentState.equals(ExperimentState.RUNNING) ||
                        currentState.equals(ExperimentState.PAUSED);
            case COMPLETED:
                return currentState.equals(ExperimentState.VALIDATING);
            case VALIDATING:
                return currentState.equals(ExperimentState.RUNNING) ||
                        currentState.equals(ExperimentState.RUNNING_STEP);
            case CONFIGURING:
                return currentState.equals(ExperimentState.INIT);
            case RUNNING_STEP:
                return currentState.equals(ExperimentState.PAUSED);
            default:
                log.debug("New state not recognized");
                return false;
        }
    }
}
