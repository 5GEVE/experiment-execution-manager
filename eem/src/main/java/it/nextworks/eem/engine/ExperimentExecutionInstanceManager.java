package it.nextworks.eem.engine;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.nextworks.eem.model.ExecutionResult;
import it.nextworks.eem.model.TestCaseExecutionConfiguration;
import it.nextworks.eem.model.enumerate.ExperimentRunType;
import it.nextworks.eem.rabbitMessage.*;
import it.nextworks.eem.model.ExperimentExecution;
import it.nextworks.eem.model.ExperimentExecutionStateChangeNotification;
import it.nextworks.eem.model.enumerate.ExperimentState;
import it.nextworks.eem.repo.ExperimentExecutionRepository;
import it.nextworks.eem.sbi.expcatalogue.ExperimentCatalogueService;
import it.nextworks.eem.sbi.jenkins.JenkinsService;
import it.nextworks.eem.sbi.msno.MsnoService;
import it.nextworks.eem.sbi.runtimeConfigurator.RunTimeConfiguratorService;
import it.nextworks.eem.sbi.validationComponent.ValidationService;
import it.nextworks.nfvmano.catalogue.blueprint.elements.*;
import it.nextworks.nfvmano.catalogue.blueprint.messages.*;
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
    private ExperimentRunType runType;

    private JenkinsService jenkinsService;
    private ValidationService validationService;
    private EemSubscriptionService subscriptionService;
    private RunTimeConfiguratorService runTimeConfiguratorService;
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
    private Map.Entry<String, String> runningTestCase;


    private String validationBaseUrl;

    public ExperimentExecutionInstanceManager(String executionId, ExperimentExecutionRepository experimentExecutionRepository, EemSubscriptionService subscriptionService, JenkinsService jenkinsService, ValidationService validationService, RunTimeConfiguratorService runTimeConfiguratorService, ExperimentCatalogueService catalogueService, MsnoService msnoService, String validationBaseUrl) throws NotExistingEntityException
    {
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        if(!experimentExecutionOptional.isPresent())
            throw new NotExistingEntityException(String.format("Experiment Execution with Id %s not found", executionId));
        this.executionId = executionId;
        this.currentState = experimentExecutionOptional.get().getState();
        this.runType = experimentExecutionOptional.get().getRunType();
        this.experimentExecutionRepository = experimentExecutionRepository;
        this.subscriptionService = subscriptionService;
        this.interruptRunning = false;
        this.jenkinsService = jenkinsService;
        this.validationService = validationService;
        this.runTimeConfiguratorService = runTimeConfiguratorService;
        this.catalogueService = catalogueService;
        this.msnoService = msnoService;
        this.validationBaseUrl = validationBaseUrl;
        //Retrieve again all information for stored experiment executions
        if(!this.currentState.equals(ExperimentState.INIT)) {
            try {
                retrieveAllInformation();
            }catch (FailedOperationException e){
                log.error(e.getMessage());
                manageExperimentExecutionError(e.getMessage());
            }
        }
        //Restart experiment executions based on the current state
        switch(currentState){
            case CONFIGURING:
                if(jenkinsService != null)//Configuration is done by Jenkins during test case execution
                    processConfigurationResult(new ConfigurationResultInternalMessage("Configuration done by Jenkins", false));
                else
                    runTimeConfiguratorService.configureExperiment(executionId);
                break;
            case RUNNING: case RUNNING_STEP:
                runExperimentExecutionTestCase();
                break;
            case VALIDATING:
                if(jenkinsService != null)//Validation is done by Jenkins during test case execution
                    processValidationResult(new ValidationResultInternalMessage("Validation done by Jenkins", false));
                else
                    validationService.validateExperiment(executionId);
                break;
            case ABORTING:
                //Consider last test case has been already aborted
                if (updateAndNotifyExperimentExecutionState(ExperimentState.ABORTED))
                    log.info("Experiment Execution with Id {} aborted", executionId);
                break;
            default:
                log.debug("There aren't pending operations for Experiment Execution with Id {}", executionId);
        }
    }

    public void setRunType(ExperimentRunType runType){
        this.runType = runType;
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
                //Messages from NBI
                case RUN: {
                    log.debug("Processing request to run Experiment Execution with Id {}", executionId);
                    processRunRequest();
                    break;
                }
                case PAUSE: {
                    log.debug("Processing request to pause Experiment Execution with Id {}", executionId);
                    processPauseRequest();
                    break;
                }
                case RESUME: {
                    log.debug("Processing request to resume Experiment Execution with Id {}", executionId);
                    processResumeRequest();
                    break;
                }
                case STEP: {
                    log.debug("Processing request to run a Test Case of Experiment Execution with Id {}", executionId);
                    processStepRequest();
                    break;
                }
                case ABORT: {
                    log.debug("Processing request to abort Experiment Execution with Id {}", executionId);
                    processAbortRequest();
                    break;
                }
                //Messages from SBI Services
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
                } case ABORTING_RESULT: {
                    AbortingResultInternalMessage msg = (AbortingResultInternalMessage) im;
                    log.debug("Processing configuration result of Experiment Execution with Id {}", executionId);
                    processAbortingResult(msg);
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
            log.debug("Unhandled Exception", e);
        }
    }

    private void processRunRequest(){
        if(updateAndNotifyExperimentExecutionState(ExperimentState.CONFIGURING)) {
            log.info("Configuring Experiment Execution with Id {}", executionId);
            try {
                retrieveAllInformation();
            }catch (FailedOperationException e){
                log.error(e.getMessage());
                manageExperimentExecutionError(e.getMessage());
                return;
            }
            if(jenkinsService != null)//Configuration is done by Jenkins during test case execution
                processConfigurationResult(new ConfigurationResultInternalMessage("Configuration done by Jenkins", false));
            else
                runTimeConfiguratorService.configureExperiment(executionId);
        }
    }

    private void processPauseRequest(){
        log.info("Pausing Experiment Execution with Id {}", executionId);
        interruptRunning = true;
    }

    private void processResumeRequest(){
        if(updateAndNotifyExperimentExecutionState(ExperimentState.RUNNING)) {
            log.info("Resuming Experiment Execution with Id {}", executionId);
            runExperimentExecutionTestCase();
            log.info("Experiment Execution with Id {} resumed", executionId);
        }
    }

    private void processStepRequest(){
        if(updateAndNotifyExperimentExecutionState(ExperimentState.RUNNING_STEP)) {
            log.info("Running a step of Experiment Execution with Id {}", executionId);
            runExperimentExecutionTestCase();
        }
    }

    private void processAbortRequest(){
        boolean abortNow = currentState.equals(ExperimentState.PAUSED);
        if(updateAndNotifyExperimentExecutionState(ExperimentState.ABORTING)) {
            log.info("Aborting Experiment Execution with Id {}", executionId);
            if(abortNow){
                //If PAUSED, there is no need to abort a test case run
                if (updateAndNotifyExperimentExecutionState(ExperimentState.ABORTED))
                    log.info("Experiment Execution with Id {} aborted", executionId);
            }
            else if(jenkinsService != null)
                jenkinsService.abortTestCase(executionId, runningTestCase.getKey());
            else
                runTimeConfiguratorService.abortTestCase(executionId, runningTestCase.getKey());
        }
    }

    private void processTestCaseResult(TestCaseResultInternalMessage msg){
        String testCaseId = msg.getTcDescriptorId();
        log.info("Processing result of Test Case with Id {} of Experiment Execution with Id {}", testCaseId, executionId);
        /*
        //If a test case run fails, all the experiment execution fails
        if(msg.isFailed()) {
            manageExperimentExecutionError(msg.getResult());
            return;
        }
         */
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        if(!experimentExecutionOptional.isPresent()){
            log.error("Experiment Execution with Id {} not found", executionId);
            return;
        }
        ExperimentExecution experimentExecution = experimentExecutionOptional.get();
        ExecutionResult executionResult = new ExecutionResult();
        executionResult.result(msg.getResult());
        experimentExecution.addTestCaseResult(testCaseId, executionResult);
        experimentExecutionRepository.saveAndFlush(experimentExecution);
        log.info("Experiment Execution Test Case with Id {} completed", testCaseId);
        //Remove completed test case from the list
        testCasesIterator.remove();
        //Abort experiment execution if requested
        if(currentState.equals(ExperimentState.ABORTING) && updateAndNotifyExperimentExecutionState(ExperimentState.ABORTED)) {
            log.info("Experiment Execution with Id {} aborted", executionId);
            return;
        }
        if(testCases.size() == 0){
            experimentExecution.reportUrl(this.validationBaseUrl + executionId + "/index.html");
            // TODO: jenkinsIP/EXEC_ID/index.html
            //Validate experiment execution if test cases are no longer present
            if(updateAndNotifyExperimentExecutionState(ExperimentState.VALIDATING)) {
                log.info("Validating Experiment Execution with Id {}", executionId);
                if(jenkinsService != null)//Validation is done by Jenkins during test case execution
                    processValidationResult(new ValidationResultInternalMessage("Validation done by Jenkins", false));
                else
                    validationService.validateExperiment(executionId);//TODO pass data to validate
            }
        }else if(currentState.equals(ExperimentState.RUNNING_STEP) || (currentState.equals(ExperimentState.RUNNING) && interruptRunning)) {
            //Pause experiment execution if the run type is RUN_IN_STEPS or if requested
            if(updateAndNotifyExperimentExecutionState(ExperimentState.PAUSED)) {
                interruptRunning = false;
                log.info("Experiment Execution with Id {} paused", executionId);
            }
        }else if(currentState.equals(ExperimentState.RUNNING)){
            //Run another test case if run type is RUN_ALL
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
        if(runType.equals(ExperimentRunType.RUN_ALL)){
            //Run the first test case if run type is RUN_ALL
            if(updateAndNotifyExperimentExecutionState(ExperimentState.RUNNING)) {
                log.info("Running Experiment Execution with Id {}", executionId);
                runExperimentExecutionTestCase();
            }
        }else {
            //Pause experiment execution if the run type is RUN_IN_STEPS
            if(updateAndNotifyExperimentExecutionState(ExperimentState.PAUSED))
                log.info("Experiment Execution with Id {} paused", executionId);
        }
        log.info("Configuration of Experiment Execution with Id {} completed", executionId);
    }

    private void processAbortingResult(AbortingResultInternalMessage msg){
        if(msg.isFailed()) {
            manageExperimentExecutionError(msg.getResult());
            return;
        }
        if (updateAndNotifyExperimentExecutionState(ExperimentState.ABORTED))
            log.info("Experiment Execution with Id {} aborted", executionId);
    }

    private void runExperimentExecutionTestCase(){
        if(testCasesIterator.hasNext()) {
            //Take the first test case from the list
            runningTestCase = testCasesIterator.next();
            String tcDescriptorId = runningTestCase.getKey();
            log.info("Running Experiment Execution Test Case with Id {}", tcDescriptorId);
            if(jenkinsService != null)
                jenkinsService.runTestCase(executionId, tcDescriptorId, runningTestCase.getValue());
            else
                runTimeConfiguratorService.runTestCase(executionId, tcDescriptorId, runningTestCase.getValue());
        }else
            log.debug("No more Test Cases to run");
    }

    private void retrieveAllInformation() throws FailedOperationException {
        log.info("Retrieving all the information for Experiment Execution with Id {}", executionId);
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        if(!experimentExecutionOptional.isPresent())
            throw new FailedOperationException(String.format("Experiment Execution with Id %s not found", executionId));
        ExperimentExecution experimentExecution = experimentExecutionOptional.get();
        String experimentDescriptorId = experimentExecution.getExperimentDescriptorId();
        String nsInstanceId = experimentExecution.getNsInstanceId();
        try{
            Map<String, String> parameters = new HashMap<>();
            Filter filter = new Filter(parameters);
            GeneralizedQueryRequest request = new GeneralizedQueryRequest(filter, null);
            //Retrieve Experiment Descriptor
            log.debug("Going to retrieve Experiment Descriptor with Id {}", experimentDescriptorId);
            parameters.put("ExpD_ID", experimentDescriptorId);
            QueryExpDescriptorResponse expDescriptorResponse = catalogueService.queryExpDescriptor(request);
            if(expDescriptorResponse.getExpDescriptors().isEmpty())
                throw new FailedOperationException(String.format("Experiment Descriptor with Id %s not found", experimentDescriptorId));
            expDescriptor = expDescriptorResponse.getExpDescriptors().get(0);
            parameters.remove("ExpD_ID");
            //Retrieve Vertical Service Descriptor
            String vsDescriptorId = expDescriptor.getVsDescriptorId();
            log.debug("Going to retrieve Vertical Service Descriptor with Id {}", vsDescriptorId);
            parameters.put("VSD_ID", vsDescriptorId);
            QueryVsDescriptorResponse vsDescriptorResponse = catalogueService.queryVsDescriptor(request);
            if(vsDescriptorResponse.getVsDescriptors().isEmpty())
                throw new FailedOperationException(String.format("Vertical Service Descriptor with Id %s not found", vsDescriptorId));
            vsDescriptor = vsDescriptorResponse.getVsDescriptors().get(0);
            parameters.remove("VSD_ID");
            //Retrieve Context Descriptors
            List<String> ctxDescriptorIds = expDescriptor.getCtxDescriptorIds();
            log.debug("Going to retrieve Context Descriptors with Ids {}", ctxDescriptorIds);
            for(String ctxDescriptorId : ctxDescriptorIds) {
                parameters.put("CTXD_ID", ctxDescriptorId);
                QueryCtxDescriptorResponse ctxDescriptorResponse = catalogueService.queryCtxDescriptor(request);
                if (ctxDescriptorResponse.getCtxDescriptors().isEmpty())
                    throw new FailedOperationException(String.format("Context Descriptor with Id %s not found", ctxDescriptorId));
                CtxDescriptor ctxDescriptor = ctxDescriptorResponse.getCtxDescriptors().get(0);
                ctxDescriptors.add(ctxDescriptor);
                parameters.remove("CTXD_ID");
            }
            //Retrieve Test Case Descriptors
            List<String> tcDescriptorIds = expDescriptor.getTestCaseDescriptorIds();
            log.debug("Going to retrieve Test Case Descriptors with Ids {}", tcDescriptorIds);
            for(String tcDescriptorId : tcDescriptorIds) {
                parameters.put("TCD_ID", tcDescriptorId);
                QueryTestCaseDescriptorResponse tcDescriptorResponse = catalogueService.queryTestCaseDescriptor(request);
                if(tcDescriptorResponse.getTestCaseDescriptors().isEmpty())
                    throw new FailedOperationException(String.format("Test Case Descriptor with Id %s not found", tcDescriptorId));
                TestCaseDescriptor tcDescriptor = tcDescriptorResponse.getTestCaseDescriptors().get(0);
                tcDescriptors.add(tcDescriptor);
                parameters.remove("TCD_ID");
            }
            //Retrieve Test Case Blueprints
            List<String> tcBlueprintIds = tcDescriptors.stream().map(TestCaseDescriptor::getTestCaseBlueprintId).collect(Collectors.toList());
            log.debug("Going to retrieve Test Case Blueprints with Ids {}", tcBlueprintIds);
            for(String tcBlueprintId : tcBlueprintIds) {
                parameters.put("TCB_ID", tcBlueprintId);
                QueryTestCaseBlueprintResponse tcBlueprintResponse = catalogueService.queryTestCaseBlueprint(request);
                if(tcBlueprintResponse.getTestCaseBlueprints().isEmpty())
                    throw new FailedOperationException(String.format("Test Case Blueprint with Id %s not found", tcBlueprintId));
                TestCaseBlueprint tcBlueprint = tcBlueprintResponse.getTestCaseBlueprints().get(0).getTestCaseBlueprint();
                tcBlueprints.add(tcBlueprint);
                parameters.remove("TCB_ID");
            }
            //Retrieve NsInstance
            log.debug("Going to retrieve NsInstance with Id {}", nsInstanceId);
            parameters.put("NS_ID", nsInstanceId);
            nsInstance = msnoService.queryNs(request);
            parameters.remove("NS_ID");
            translateTestCases();
        }catch (MalformattedElementException e){
            throw new FailedOperationException(e.getMessage());
        }
    }

    private void translateTestCases() throws FailedOperationException{
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        if(!experimentExecutionOptional.isPresent())
            throw new FailedOperationException(String.format("Experiment Execution with Id %s not found", executionId));
        ExperimentExecution experimentExecution = experimentExecutionOptional.get();
        //Override user parameters inside test case descriptor
        List<TestCaseExecutionConfiguration> executionConfigurations = experimentExecution.getTestCaseDescriptorConfiguration();
        for(TestCaseExecutionConfiguration executionConfiguration : executionConfigurations)
            for(TestCaseDescriptor tcDescriptor : tcDescriptors)
                if(tcDescriptor.getTestCaseDescriptorId().equals(executionConfiguration.getTcDescriptorId())) {
                    log.debug("Replacing userParameters {} with executionConfigurations {} for Test Case with Id {}", tcDescriptor.getUserParameters(), executionConfiguration.getExecConfiguration(), tcDescriptor.getTestCaseDescriptorId());
                    executionConfiguration.getExecConfiguration().forEach((x, y) -> tcDescriptor.getUserParameters().replace(x, y));
                }
        // TODO: Aggiungere i parametri di traduzione
        if(jenkinsService != null) {
            //TODO create robot files
        }else{
            //TODO create test case files
        }
        //Put in the list only test cases not completed, i.e. we have no results of
        Set<String> executionResultIds = experimentExecution.getTestCaseResult().keySet();//TODO put only test cases not present inside testCaseResult of experiment Execution (needed for resuming the experiment)
        //TODO remove
        if(!executionResultIds.contains("testCase1"))
            testCases.put("testCase1", "test");
        if(!executionResultIds.contains("testCase2"))
            testCases.put("testCase2", "test");
        if(!executionResultIds.contains("testCase3"))
            testCases.put("testCase3", "test");
        //Initialize test case iterator
        testCasesIterator = testCases.entrySet().iterator();
    }

    private void manageExperimentExecutionError(String errorMessage){
        log.error("Experiment Execution with Id {} failed : {}", executionId, errorMessage);
        if(updateAndNotifyExperimentExecutionState(ExperimentState.FAILED)) {
            Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
            experimentExecutionOptional.ifPresent(experimentExecution -> experimentExecutionRepository.saveAndFlush(experimentExecution.errorMessage(errorMessage)));
        }
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
        //Send notification to subscribers
        ExperimentExecutionStateChangeNotification msg = new ExperimentExecutionStateChangeNotification(executionId, currentState);
        subscriptionService.notifyExperimentExecutionStateChange(msg, previousState);
        return true;
    }

    private boolean isStateChangeAllowed(ExperimentState currentState, ExperimentState newState){
        //Map allowed state change
        switch (newState){
            case FAILED:
                return  currentState.equals(ExperimentState.CONFIGURING) ||
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
                        (currentState.equals(ExperimentState.PAUSED) && runType.equals(ExperimentRunType.RUN_ALL));
            case ABORTING:
                return currentState.equals(ExperimentState.RUNNING) ||
                        currentState.equals(ExperimentState.RUNNING_STEP) ||
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
