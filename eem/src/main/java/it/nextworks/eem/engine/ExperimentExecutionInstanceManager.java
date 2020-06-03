package it.nextworks.eem.engine;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.nextworks.eem.model.*;
import it.nextworks.eem.model.enumerate.ExperimentExecutionResultCode;
import it.nextworks.eem.model.enumerate.ExperimentRunType;
import it.nextworks.eem.model.enumerate.InfrastructureParameterType;
import it.nextworks.eem.rabbitMessage.*;
import it.nextworks.eem.model.enumerate.ExperimentState;
import it.nextworks.eem.repo.ExperimentExecutionRepository;
import it.nextworks.eem.sbi.ConfiguratorService;
import it.nextworks.eem.sbi.ExecutorService;
import it.nextworks.eem.sbi.MultiSiteOrchestratorService;
import it.nextworks.eem.sbi.ValidatorService;
import it.nextworks.eem.sbi.expcatalogue.ExperimentCatalogueService;
import it.nextworks.eem.sbi.rav.model.MetricDataType;
import it.nextworks.nfvmano.catalogue.blueprint.elements.*;
import it.nextworks.nfvmano.catalogue.blueprint.messages.*;
import it.nextworks.nfvmano.libs.ifa.common.elements.Filter;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.common.messages.GeneralizedQueryRequest;
import it.nextworks.openapi.msno.model.*;
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

    private ConfiguratorService configuratorService;
    private ExecutorService executorService;
    private ValidatorService validatorService;

    private EemSubscriptionService subscriptionService;
    private ExperimentCatalogueService catalogueService;
    private MultiSiteOrchestratorService multiSiteOrchestratorService;
    private ExperimentExecutionRepository experimentExecutionRepository;

    private boolean interruptRunning;
    private boolean isValidationConfigured;
    private boolean isTestCaseConfigured;

    private ExpBlueprint expBlueprint;
    private ExpDescriptor expDescriptor;
    private VsBlueprint vsBlueprint;
    private VsDescriptor vsDescriptor;
    private List<CtxDescriptor> ctxDescriptors = new ArrayList<>();
    private List<CtxBlueprint> ctxBlueprints = new ArrayList<>();
    private List<TestCaseDescriptor> tcDescriptors = new ArrayList<>();
    private List<TestCaseBlueprint> tcBlueprints = new ArrayList<>();
    private NsInstance nsInstance = null;

    //Key: tcDescriptorId, Value: Key: execScript|configScript|resetScript, Value: script
    private Map<String, Map<String, String>> testCases = new LinkedHashMap<>();
    private Iterator<Map.Entry<String, Map<String, String>>> testCasesIterator;
    private Map.Entry<String, Map<String, String>> runningTestCase;
    private String configId;
    private String metricConfigId;

    public ExperimentExecutionInstanceManager(String executionId, ExperimentExecutionRepository experimentExecutionRepository, EemSubscriptionService subscriptionService, ConfiguratorService configuratorService, ExecutorService executorService, ValidatorService validatorService, ExperimentCatalogueService catalogueService, MultiSiteOrchestratorService multiSiteOrchestratorService) throws NotExistingEntityException
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
        this.isTestCaseConfigured = false;
        this.isValidationConfigured = false;
        this.configuratorService = configuratorService;
        this.executorService = executorService;
        this.validatorService = validatorService;
        this.catalogueService = catalogueService;
        this.multiSiteOrchestratorService = multiSiteOrchestratorService;
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
                validatorService.configureExperiment(executionId);
                if(runType.equals(ExperimentRunType.RUN_ALL)){
                    //Run the first test case if run type is RUN_ALL
                    configureExperimentExecutionTestCase();
                } else {
                    //Pause experiment execution if the run type is RUN_IN_STEPS
                    if(updateAndNotifyExperimentExecutionState(ExperimentState.PAUSED))
                        log.info("Experiment Execution with Id {} paused", executionId);
                }
                break;
            case RUNNING: case RUNNING_STEP:
                //It is necessary to reconfigure the test case
                if(updateAndNotifyExperimentExecutionState(ExperimentState.CONFIGURING))
                    configureExperimentExecutionTestCase();
                break;
            case VALIDATING:
                //Consider startTcValidation already sent
                if(testCasesIterator.hasNext()) {
                    runningTestCase = testCasesIterator.next();
                    validatorService.stopTcValidation(executionId, runningTestCase.getKey());
                }
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
            log.debug("Error while parsing message", e);
            manageExperimentExecutionError("Error while parsing message: " + e.getMessage());
        } catch (JsonMappingException e) {
            log.debug("Error in Json mapping", e);
            manageExperimentExecutionError("Error in Json mapping: " + e.getMessage());
        } catch (IOException e) {
            log.debug("IO error when receiving json message", e);
            manageExperimentExecutionError("IO error when receiving json message: " + e.getMessage());
        } catch (Exception e){
            log.debug("Unhandled Exception", e);
            manageExperimentExecutionError("Generic internal error: " + e.getMessage());
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
            validatorService.configureExperiment(executionId);
            if(runType.equals(ExperimentRunType.RUN_ALL)){
                //Run the first test case if run type is RUN_ALL
                log.info("Running Experiment Execution with Id {}", executionId);
                configureExperimentExecutionTestCase();
            } else {
                //Pause experiment execution if the run type is RUN_IN_STEPS
                if(updateAndNotifyExperimentExecutionState(ExperimentState.PAUSED))
                    log.info("Experiment Execution with Id {} paused", executionId);
            }
        }
    }

    private void processPauseRequest(){
        log.info("Pausing Experiment Execution with Id {}", executionId);
        interruptRunning = true;
    }

    private void processResumeRequest(){
        log.info("Resuming Experiment Execution with Id {}", executionId);
        if(updateAndNotifyExperimentExecutionState(ExperimentState.CONFIGURING))
            configureExperimentExecutionTestCase();
    }

    private void processStepRequest(){
        log.info("Running a step of Experiment Execution with Id {}", executionId);
        if(updateAndNotifyExperimentExecutionState(ExperimentState.CONFIGURING))
            configureExperimentExecutionTestCase();
    }

    private void processAbortRequest(){
        ExperimentState oldState = currentState;
        if(updateAndNotifyExperimentExecutionState(ExperimentState.ABORTING)) {
            log.info("Aborting Experiment Execution with Id {}", executionId);
            if(oldState.equals(ExperimentState.PAUSED)){
                //If PAUSED, there is no need to abort a test case run
                if (updateAndNotifyExperimentExecutionState(ExperimentState.ABORTED))
                    log.info("Experiment Execution with Id {} aborted", executionId);
            }else if (oldState.equals(ExperimentState.CONFIGURING)){
                //If CONFIGURING, abort Test Case configuration
                if (updateAndNotifyExperimentExecutionState(ExperimentState.ABORTED)) {
                    configuratorService.abortConfiguration(executionId, runningTestCase.getKey(), configId);
                    log.info("Experiment Execution with Id {} aborted", executionId);
                }
            }
            else
                executorService.abortTestCase(executionId, runningTestCase.getKey());
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
        for(TestCaseDescriptor tcDescriptor : tcDescriptors)
            if(tcDescriptor.getTestCaseDescriptorId().equalsIgnoreCase(testCaseId))
                executionResult.setTestCaseName(tcDescriptor.getName());
        if(msg.isFailed())
            executionResult.setResultCode(ExperimentExecutionResultCode.FAILED);
        else
            executionResult.setResultCode(ExperimentExecutionResultCode.SUCCESSFUL);
        executionResult.setResult(msg.getResult());
        experimentExecution.addTestCaseResult(testCaseId, executionResult);
        experimentExecutionRepository.saveAndFlush(experimentExecution);
        log.info("Test Case with Id {} of Experiment Execution with Id {} completed", testCaseId, executionId);
        if(metricConfigId == null)
            processConfigurationResult(new ConfigurationResultInternalMessage(ConfigurationStatus.METRIC_RESET, "OK", null, false));
        else
            configuratorService.removeInfrastructureMetricCollection(executionId, testCaseId, metricConfigId);
    }

    private void processValidationResult(ValidationResultInternalMessage msg){
        if(msg.isFailed()) {
            manageExperimentExecutionError(msg.getResult());
            return;
        }
        switch (msg.getValidationStatus()){
            case CONFIGURED:
                log.info("Validator configured for Experiment Execution with Id {}", executionId);
                isValidationConfigured = true;
                if(isTestCaseConfigured)
                    processConfigurationResult(new ConfigurationResultInternalMessage(ConfigurationStatus.METRIC_CONFIGURED, "Validation configured", null,false));
                break;
            case ACQUIRING:
                String execScript = runningTestCase.getValue().get("execScript");
                log.debug("Execution Script: {}", execScript);
                executorService.runTestCase(executionId, runningTestCase.getKey(), execScript);
                break;
            case VALIDATING:
                validatorService.queryValidationResult(executionId, runningTestCase.getKey());
                break;
            case VALIDATED:
                Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
                if(!experimentExecutionOptional.isPresent()){
                    log.error("Experiment Execution with Id {} not found", executionId);
                    return;
                }
                ExperimentExecution experimentExecution = experimentExecutionOptional.get();
                testCasesIterator.remove();
                if(testCases.size() == 0){
                    experimentExecution.setReportUrl(msg.getResult());
                    experimentExecutionRepository.saveAndFlush(experimentExecution);
                    //Complete experiment execution if test cases are no longer present
                    if(updateAndNotifyExperimentExecutionState(ExperimentState.COMPLETED)) {
                        log.info("Experiment Execution with Id {} completed", executionId);
                        validatorService.terminateExperiment(executionId);
                    }
                }else if(runType.equals(ExperimentRunType.RUN_IN_STEPS) || (runType.equals(ExperimentRunType.RUN_ALL) && interruptRunning)) {
                    //Pause experiment execution if the run type is RUN_IN_STEPS or if requested
                    if(updateAndNotifyExperimentExecutionState(ExperimentState.PAUSED)) {
                        interruptRunning = false;
                        log.info("Experiment Execution with Id {} paused", executionId);
                    }
                }else if(runType.equals(ExperimentRunType.RUN_ALL)){
                    //Run another test case if run type is RUN_ALL
                    if(updateAndNotifyExperimentExecutionState(ExperimentState.CONFIGURING))
                        configureExperimentExecutionTestCase();
                }else {
                    log.debug("State of the Execution Experiment is not correct");
                }
        }
    }

    private void processConfigurationResult(ConfigurationResultInternalMessage msg){
        if(msg.isFailed()) {
            manageExperimentExecutionError(msg.getResult());
            return;
        }
        switch (msg.getConfigurationStatus()){
            case CONFIGURED:
                log.info("Configuration of Test Case with Id {} of Experiment Execution with Id {} completed", runningTestCase.getKey(), executionId);
                configId = msg.getConfigId();
                try {
                    configureInfrastructureMetrics();
                }catch (FailedOperationException e){
                    log.error(e.getMessage());
                    manageExperimentExecutionError(e.getMessage());
                }
                break;
            case METRIC_CONFIGURED:
                log.debug("Configuration of Infrastructure Metrics completed for Test Case with Id {}", runningTestCase.getKey());
                isTestCaseConfigured = true;
                metricConfigId = msg.getConfigId();
                if(isValidationConfigured){
                    if(runType.equals(ExperimentRunType.RUN_ALL)) {
                        if (updateAndNotifyExperimentExecutionState(ExperimentState.RUNNING))
                            runExperimentExecutionTestCase();
                    }else{
                        if (updateAndNotifyExperimentExecutionState(ExperimentState.RUNNING_STEP))
                            runExperimentExecutionTestCase();
                    }
                }
                break;
            case METRIC_RESET:
                if(configId == null)
                    processConfigurationResult(new ConfigurationResultInternalMessage(ConfigurationStatus.CONF_RESET, "OK", null, false));
                else
                    configuratorService.resetConfiguration(executionId, runningTestCase.getKey(), configId);
                break;
            case CONF_RESET:
                //Abort experiment execution if requested
                if(currentState.equals(ExperimentState.ABORTING) && updateAndNotifyExperimentExecutionState(ExperimentState.ABORTED)) {
                    log.info("Experiment Execution with Id {} aborted", executionId);
                    return;
                }
                if(updateAndNotifyExperimentExecutionState(ExperimentState.VALIDATING)) {
                    log.info("Validating Test Case with Id {} of Experiment Execution with Id {}", runningTestCase.getKey(), executionId);
                    validatorService.stopTcValidation(executionId, runningTestCase.getKey());
                }
        }
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
        log.info("Running Test Case with Id {} of Experiment Execution with Id {} ", runningTestCase.getKey(), executionId);
        isTestCaseConfigured = false;
        validatorService.startTcValidation(executionId, runningTestCase.getKey());
    }

    private void configureExperimentExecutionTestCase(){
        if(testCasesIterator.hasNext()) {
            //Take the first test case from the list
            runningTestCase = testCasesIterator.next();
            String tcDescriptorId = runningTestCase.getKey();
            log.info("Configuring Test Case with Id {} of Experiment Execution with Id {}", tcDescriptorId, executionId);
            String configScript = runningTestCase.getValue().get("configScript");
            log.debug("Configuration Script: {}", configScript);
            String resetScript = runningTestCase.getValue().get("resetScript");
            log.debug("reset Configuration Script: {}", resetScript);
            if(configScript == null || configScript.equals("")){
                ConfigurationResultInternalMessage msg = new ConfigurationResultInternalMessage(ConfigurationStatus.CONFIGURED, "OK", null, false);
                processConfigurationResult(msg);
            }else
                configuratorService.applyConfiguration(executionId, tcDescriptorId, configScript, resetScript);
        }else
            log.debug("No more Test Cases to run for Experiment Execution with Id {}", executionId);
    }

    private void configureInfrastructureMetrics() throws FailedOperationException{
        List<MetricInfo> metrics = new ArrayList<>();
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        if(!experimentExecutionOptional.isPresent())
            throw new FailedOperationException(String.format("Experiment Execution with Id %s not found", executionId));
        ExperimentExecution experimentExecution = experimentExecutionOptional.get();
        EveSite siteName;//TODO handle multiple sites performing multiple requests or better sending a list of sites
        try {
            siteName = EveSite.valueOf(experimentExecution.getSiteNames().get(0).toUpperCase());
        }catch (IllegalArgumentException e){
            throw new FailedOperationException(String.format("Incorrect site name in the Experiment Execution with Id %s", executionId));
        }
        // Infrastructure metrics from Experiment blueprint
        for (InfrastructureMetric im : expBlueprint.getMetrics()){
            String topic = experimentExecution.getUseCase() + "." + experimentExecution.getExperimentId() + "." + experimentExecution.getSiteNames().get(0).toLowerCase() + "." + MetricDataType.INFRASTRUCTURE_METRIC.toString().toLowerCase() + "." + im.getMetricId();
            MetricInfo metric = new MetricInfo(im, topic, siteName);
            metrics.add(metric);
        }
        if(!metrics.isEmpty())
            configuratorService.configureInfrastructureMetricCollection(executionId, runningTestCase.getKey(), metrics);
        else {
            ConfigurationResultInternalMessage msg = new ConfigurationResultInternalMessage(ConfigurationStatus.METRIC_CONFIGURED, "OK", null, false);
            processConfigurationResult(msg);
        }
    }

    private void retrieveAllInformation() throws FailedOperationException {
        log.info("Retrieving all the information for Experiment Execution with Id {}", executionId);
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        if(!experimentExecutionOptional.isPresent())
            throw new FailedOperationException(String.format("Experiment Execution with Id %s not found", executionId));
        ExperimentExecution experimentExecution = experimentExecutionOptional.get();
        String experimentDescriptorId = experimentExecution.getExperimentDescriptorId();
        String nsInstanceId = experimentExecution.getNsInstanceId();
        try {
            Map<String, String> parameters = new HashMap<>();
            Filter filter = new Filter(parameters);
            GeneralizedQueryRequest request = new GeneralizedQueryRequest(filter, null);
            //Retrieve Experiment Descriptor
            log.debug("Going to retrieve Experiment Descriptor with Id {}", experimentDescriptorId);
            parameters.put("ExpD_ID", experimentDescriptorId);
            QueryExpDescriptorResponse expDescriptorResponse = catalogueService.queryExpDescriptor(request);
            if (expDescriptorResponse.getExpDescriptors().isEmpty())
                throw new FailedOperationException(String.format("Experiment Descriptor with Id %s not found", experimentDescriptorId));
            expDescriptor = expDescriptorResponse.getExpDescriptors().get(0);
            parameters.remove("ExpD_ID");
            //Retrieve Experiment Blueprint
            log.debug("Going to retrieve Experiment Blueprint with Id {}", expDescriptor.getExpBlueprintId());
            parameters.put("ExpB_ID", expDescriptor.getExpBlueprintId());
            QueryExpBlueprintResponse expBlueprintResponse = catalogueService.queryExpBlueprint(request);
            if (expBlueprintResponse.getExpBlueprintInfo().isEmpty())
                throw new FailedOperationException(String.format("Experiment Blueprint with Id %s not found", expDescriptor.getExpBlueprintId()));
            expBlueprint = expBlueprintResponse.getExpBlueprintInfo().get(0).getExpBlueprint();
            parameters.remove("ExpB_ID");
            //Retrieve Vertical Service Blueprint
            log.debug("Going to retrieve Vertical Service Blueprint with Id {}", expBlueprint.getVsBlueprintId());
            parameters.put("VSB_ID", expBlueprint.getVsBlueprintId());
            QueryVsBlueprintResponse vsBlueprintResponse = catalogueService.queryVsBlueprint(request);
            if (vsBlueprintResponse.getVsBlueprintInfo().isEmpty())
                throw new FailedOperationException(String.format("Vertical Service Blueprint with Id %s not found", expBlueprint.getVsBlueprintId()));
            vsBlueprint = vsBlueprintResponse.getVsBlueprintInfo().get(0).getVsBlueprint();
            parameters.remove("VSB_ID");
            //Retrieve Vertical Service Descriptor
            String vsDescriptorId = expDescriptor.getVsDescriptorId();
            log.debug("Going to retrieve Vertical Service Descriptor with Id {}", vsDescriptorId);
            parameters.put("VSD_ID", vsDescriptorId);
            QueryVsDescriptorResponse vsDescriptorResponse = catalogueService.queryVsDescriptor(request);
            if (vsDescriptorResponse.getVsDescriptors().isEmpty())
                throw new FailedOperationException(String.format("Vertical Service Descriptor with Id %s not found", vsDescriptorId));
            vsDescriptor = vsDescriptorResponse.getVsDescriptors().get(0);
            parameters.remove("VSD_ID");
            //Retrieve Context Blueprints
            List<String> ctxBlueprintsIds = expBlueprint.getCtxBlueprintIds();
            if (ctxBlueprintsIds != null){
                log.debug("Going to retrieve Context Blueprints with Ids {}", ctxBlueprintsIds);
                for (String ctxBId : ctxBlueprintsIds) {
                    parameters.put("CTXB_ID", ctxBId);
                    QueryCtxBlueprintResponse ctxBlueprintResponse = catalogueService.queryCtxBlueprint(request);
                    if (ctxBlueprintResponse.getCtxBlueprintInfos().isEmpty())
                        throw new FailedOperationException(String.format("Context Blueprint with Id %s not found", ctxBId));
                    CtxBlueprint ctxBlueprint = ctxBlueprintResponse.getCtxBlueprintInfos().get(0).getCtxBlueprint();
                    ctxBlueprints.add(ctxBlueprint);
                    parameters.remove("CTXB_ID");
                }
            }
            //Retrieve Context Descriptors
            List<String> ctxDescriptorIds = expDescriptor.getCtxDescriptorIds();
            if(ctxDescriptorIds != null) {
                log.debug("Going to retrieve Context Descriptors with Ids {}", ctxDescriptorIds);
                for (String ctxDescriptorId : ctxDescriptorIds) {
                    parameters.put("CTXD_ID", ctxDescriptorId);
                    QueryCtxDescriptorResponse ctxDescriptorResponse = catalogueService.queryCtxDescriptor(request);
                    if (ctxDescriptorResponse.getCtxDescriptors().isEmpty())
                        throw new FailedOperationException(String.format("Context Descriptor with Id %s not found", ctxDescriptorId));
                    CtxDescriptor ctxDescriptor = ctxDescriptorResponse.getCtxDescriptors().get(0);
                    ctxDescriptors.add(ctxDescriptor);
                    parameters.remove("CTXD_ID");
                }
            }
            //Retrieve Test Case Descriptors
            List<String> tcDescriptorIds = expDescriptor.getTestCaseDescriptorIds();
            if(tcDescriptorIds != null) {
                log.debug("Going to retrieve Test Case Descriptors with Ids {}", tcDescriptorIds);
                for (String tcDescriptorId : tcDescriptorIds) {
                    parameters.put("TCD_ID", tcDescriptorId);
                    QueryTestCaseDescriptorResponse tcDescriptorResponse = catalogueService.queryTestCaseDescriptor(request);
                    if (tcDescriptorResponse.getTestCaseDescriptors().isEmpty())
                        throw new FailedOperationException(String.format("Test Case Descriptor with Id %s not found", tcDescriptorId));
                    TestCaseDescriptor tcDescriptor = tcDescriptorResponse.getTestCaseDescriptors().get(0);
                    tcDescriptors.add(tcDescriptor);
                    parameters.remove("TCD_ID");
                }
            }
            //Retrieve Test Case Blueprints
            List<String> tcBlueprintIds = tcDescriptors.stream().map(TestCaseDescriptor::getTestCaseBlueprintId).collect(Collectors.toList());
            if(tcBlueprintIds != null) {
                log.debug("Going to retrieve Test Case Blueprints with Ids {}", tcBlueprintIds);
                for (String tcBlueprintId : tcBlueprintIds) {
                    parameters.put("TCB_ID", tcBlueprintId);
                    QueryTestCaseBlueprintResponse tcBlueprintResponse = catalogueService.queryTestCaseBlueprint(request);
                    if (tcBlueprintResponse.getTestCaseBlueprints().isEmpty())
                        throw new FailedOperationException(String.format("Test Case Blueprint with Id %s not found", tcBlueprintId));
                    TestCaseBlueprint tcBlueprint = tcBlueprintResponse.getTestCaseBlueprints().get(0).getTestCaseBlueprint();
                    tcBlueprints.add(tcBlueprint);
                    parameters.remove("TCB_ID");
                }
            }
            //Retrieve NsInstance
            if(nsInstanceId != null) {
                log.debug("Going to retrieve NsInstance with Id {}", nsInstanceId);
                parameters.put("NS_ID", nsInstanceId);
                nsInstance = multiSiteOrchestratorService.queryNs(request);
                parameters.remove("NS_ID");
            }
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
        Set<String> executionResultIds = experimentExecution.getTestCaseResult().keySet();
        //Override user parameters inside the test case descriptor
        List<TestCaseExecutionConfiguration> executionConfigurations = experimentExecution.getTestCaseDescriptorConfiguration();
        for(TestCaseExecutionConfiguration executionConfiguration : executionConfigurations) {
            for (TestCaseDescriptor tcDescriptor : tcDescriptors) {
                if (tcDescriptor.getTestCaseDescriptorId().equals(executionConfiguration.getTcDescriptorId())) {
                    log.debug("Replacing userParameters {} with executionConfigurations {} for Test Case Descriptor with Id {}", tcDescriptor.getUserParameters(), executionConfiguration.getExecConfiguration(), tcDescriptor.getTestCaseDescriptorId());
                    executionConfiguration.getExecConfiguration().forEach((parameterName, parameterValue) -> tcDescriptor.getUserParameters().replace(parameterName, parameterValue));
                }
            }
        }
        for(TestCaseBlueprint tcBlueprint: tcBlueprints){
            //Override infrastructure parameters inside script fields in the test case blueprint
            String executionScript = overrideInfrastructureParameters(tcBlueprint, tcBlueprint.getExecutionScript());
            String configurationScript = overrideInfrastructureParameters(tcBlueprint, tcBlueprint.getConfigurationScript());
            String resetConfigScript = overrideInfrastructureParameters(tcBlueprint, tcBlueprint.getResetConfigScript());
            for(TestCaseDescriptor tcDescriptor : tcDescriptors){
                if(tcBlueprint.getTestcaseBlueprintId().equalsIgnoreCase(tcDescriptor.getTestCaseBlueprintId())){
                    //Override user parameters inside script fields in the test case blueprint
                    executionScript = overrideUserParameters(tcBlueprint, tcDescriptor, executionScript);
                    configurationScript = overrideUserParameters(tcBlueprint, tcDescriptor, configurationScript);
                    resetConfigScript = overrideUserParameters(tcBlueprint, tcDescriptor, resetConfigScript);
                    //Add only if test case has not been already executed
                    if(!executionResultIds.contains(tcDescriptor.getTestCaseDescriptorId())) {
                        Map<String, String> scripts = new LinkedHashMap<>();
                        scripts.put("execScript", executionScript);
                        scripts.put("configScript", configurationScript);
                        scripts.put("resetScript", resetConfigScript);
                        testCases.put(tcDescriptor.getTestCaseDescriptorId(), scripts);
                    }
                }
            }
        }
        testCasesIterator = testCases.entrySet().iterator();
    }

    private String overrideInfrastructureParameters(TestCaseBlueprint tcBlueprint, String script) throws FailedOperationException{
        if(script == null)
            return null;
        for (Map.Entry<String, String> infrastructureParameterEntry : tcBlueprint.getInfrastructureParameters().entrySet()) {
            script = script.replace(infrastructureParameterEntry.getKey(),
                    findInfrastructureParameterValue(infrastructureParameterEntry.getKey()));
        }
        return script;
    }

    private String overrideUserParameters(TestCaseBlueprint tcBlueprint, TestCaseDescriptor tcDescriptor, String script){
        if(script == null)
            return null;
        for(Map.Entry<String, String> userParameterEntry : tcDescriptor.getUserParameters().entrySet())
            script = script.replace(tcBlueprint.getUserParameters().get(userParameterEntry.getKey()), userParameterEntry.getValue());
        return script;
    }

    private String findInfrastructureParameterValue(String infrastructureParameter) throws FailedOperationException{
        //format example:
        //sap.<sap_id>.ipaddress
        //vnf.<vnfd_id>.extcp.<extcp_id>.ipaddress
        //vnf.<vnfd_id>.vdu.<vdu_id>.cp.<cp_id>.ipaddress
        //pnf.<pnfd_id>.cp.<cp_id>.ipaddress
        //$$metric.topic.metricid
        //$$metric.site.metricid
        //$$metric.unit.metricid
        //$$metric.interval.metricid
        String infrastructureParameterValue = null;
        String [] splits = infrastructureParameter.split("\\.");
        List<String> ids = new ArrayList<>();
        if (infrastructureParameter.startsWith("sap")) {
            log.debug("Infrastructure parameter related to NS");
            ids.add(splits[1]);
            if(splits.length == 3) {
                if (splits[2].equalsIgnoreCase("ipaddress")) {
                    try {
                        infrastructureParameterValue = readParameter(InfrastructureParameterType.SAP_IP_ADDRESS, ids);
                    } catch (FailedOperationException e) {
                        log.error("Unable to get SAP ip address. Skipping");
                        log.debug("Unable to get SAP ip address", e);
                    }
                } else
                    log.error("Unacceptable Infrastructure parameter format: {}. Skipping", infrastructureParameter);
            } else
                log.error("Unacceptable Infrastructure parameter format: {}. Skipping", infrastructureParameter);
        } else if(infrastructureParameter.startsWith("vnf")) {
            ids.add(splits[1]);
            if (splits.length == 5) {
                log.debug("Infrastructure parameter related to VNF");
                if (splits[2].equalsIgnoreCase("extcp") && splits[4].equalsIgnoreCase("ipaddress")) {
                    try {
                        ids.add(splits[3]);
                        infrastructureParameterValue = readParameter(InfrastructureParameterType.VNF_CP_IP_ADDRESS, ids);
                    } catch (FailedOperationException e) {
                        log.error("Unable to get VNF ExtCp ip address. Skipping");
                        log.debug("Unable to get VNF ExtCp ip address", e);
                    }
                } else
                    log.error("Unacceptable Infrastructure parameter format: {}. Skipping ", infrastructureParameter);
            } else if (splits.length == 7 && splits[2].equalsIgnoreCase("vdu")) {
                log.debug("Infrastructure parameter related to VNF VDU");
                ids.add(splits[3]);
                if (splits[4].equalsIgnoreCase("cp") && splits[6].equalsIgnoreCase("ipaddress")) {
                    try {
                        ids.add(splits[5]);
                        infrastructureParameterValue = readParameter(InfrastructureParameterType.VDU_CP_IP_ADDRESS, ids);
                    } catch (FailedOperationException e) {
                        log.error("Unable to get CP Address. Skipping");
                        log.debug("Unable to get CP Address", e);
                    }
                } else
                    log.error("Unacceptable Infrastructure parameter format: {}. Skipping ", infrastructureParameter);
            } else
                log.error("Unacceptable Infrastructure parameter format: {}. Skipping", infrastructureParameter);
        } else if(infrastructureParameter.startsWith("pnf")) {
            ids.add(splits[1]);
            if(splits.length == 5) {
                if(splits[2].equalsIgnoreCase("cp")) {
                    try {
                        ids.add(splits[3]);
                        infrastructureParameterValue = readParameter(InfrastructureParameterType.PNF_CP_IP_ADDRESS, ids);
                    } catch (FailedOperationException e) {
                        log.error("Unable to get Hostname. Skipping");
                        log.debug("Unable to get Hostname", e);
                    }
                }else
                    log.error("Unacceptable Infrastructure parameter format: {}. Skipping", infrastructureParameter);
            } else
                log.error("Unacceptable Infrastructure parameter format: {}. Skipping", infrastructureParameter);
        }else if(infrastructureParameter.startsWith("$$metric")){
            log.debug("Infrastructure parameter related to application metrics");
            Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
            if(!experimentExecutionOptional.isPresent())
                throw new FailedOperationException(String.format("Experiment Execution with Id %s not found", executionId));
            ExperimentExecution experimentExecution = experimentExecutionOptional.get();
            String metricId = splits[2];
            //Check application metrics from Context Blueprints
            for (CtxBlueprint ctxB : ctxBlueprints) {
                if(ctxB.getApplicationMetrics() != null) {
                    for (ApplicationMetric amd : ctxB.getApplicationMetrics()) {
                        if (amd.getMetricId().equalsIgnoreCase(metricId)) {
                            switch (splits[1]) {
                                case "topic":
                                    infrastructureParameterValue = experimentExecution.getUseCase() + "." + experimentExecution.getExperimentId() + "." + experimentExecution.getSiteNames().get(0).toLowerCase() + "." + MetricDataType.APPLICATION_METRIC.toString().toLowerCase() + "." + amd.getMetricId();
                                    break;
                                case "site":
                                    infrastructureParameterValue = experimentExecution.getSiteNames().get(0);
                                    break;
                                case "unit":
                                    infrastructureParameterValue = amd.getUnit();
                                    break;
                                case "interval":
                                    infrastructureParameterValue = amd.getInterval();
                            }
                            break;
                        }
                    }
                }
            }
            //Check application metrics from VS Blueprint
            if(infrastructureParameterValue == null) {
                if(vsBlueprint.getApplicationMetrics() != null) {
                    for (ApplicationMetric amd : vsBlueprint.getApplicationMetrics()) {
                        if (amd.getMetricId().equalsIgnoreCase(metricId)) {
                            switch (splits[1]) {
                                case "topic":
                                    infrastructureParameterValue = experimentExecution.getUseCase() + "." + experimentExecution.getExperimentId() + "." + experimentExecution.getSiteNames().get(0).toLowerCase() + "." + MetricDataType.APPLICATION_METRIC.toString().toLowerCase() + "." + amd.getMetricId();
                                    break;
                                case "site":
                                    infrastructureParameterValue = experimentExecution.getSiteNames().get(0);
                                    break;
                                case "unit":
                                    infrastructureParameterValue = amd.getUnit();
                                    break;
                                case "interval":
                                    infrastructureParameterValue = amd.getInterval();
                            }
                            break;
                        }
                    }
                }
            }
        } else
            log.error("Unacceptable Infrastructure parameter format: {}. Skipping", infrastructureParameter);

        if(infrastructureParameterValue == null)
            throw new FailedOperationException("Cannot find the value for the Infrastructure Parameter " + infrastructureParameter);

        return infrastructureParameterValue;
    }

    private String readParameter(InfrastructureParameterType parameterType, List<String> ids) throws FailedOperationException{
        //TODO add try/catch block to handle NullPointerException?
        if(nsInstance == null)
            throw new FailedOperationException("Cannot obtain infrastructure parameters information: Ns instance is null");
        String parameterValue = null;
        switch (parameterType) {
            case SAP_IP_ADDRESS:
                log.debug("Finding IP address of SAP with ID {}", ids.get(0));
                for(SapInfo sapInfo : nsInstance.getSapInfo())
                    if (sapInfo.getSapdId().equals(ids.get(0))) //TODO check if it is getId instead
                        parameterValue = getIpAddress(sapInfo.getSapProtocolInfo());
                if(parameterValue == null)
                    throw new FailedOperationException("Unable to find the SAP IP address");
                return parameterValue;
            case VNF_CP_IP_ADDRESS:
                log.debug("Finding IP address of Connection Point with ID {} for VNF with ID {}", ids.get(1), ids.get(0));
                for(VnfInstance vnfInstance : nsInstance.getVnfInstance())
                    if(vnfInstance.getVnfdId().equalsIgnoreCase(ids.get(0)))
                        for(VnfExtCpInfo vnfExtCpInfo : vnfInstance.getInstantiatedVnfInfo().getExtCpInfo())
                            if (vnfExtCpInfo.getCpdId().equalsIgnoreCase(ids.get(1)))
                                parameterValue = getIpAddress(vnfExtCpInfo.getCpProtocolInfo());
                if(parameterValue == null)
                    throw new FailedOperationException("Unable to find the VNF Connection Point IP address");
                return parameterValue;
            case VDU_CP_IP_ADDRESS:
                log.debug("Finding IP address of Connection Point with ID {} for VDU with ID {}", ids.get(2), ids.get(1));
                for(VnfInstance vnfInstance : nsInstance.getVnfInstance())
                    if (vnfInstance.getVnfdId().equalsIgnoreCase(ids.get(0)))
                        for (VnfcResourceInfo vnfcResourceInfo : vnfInstance.getInstantiatedVnfInfo().getVnfcResourceInfo())
                            if(vnfcResourceInfo.getVduId().equalsIgnoreCase(ids.get(1)))
                                for(VnfcResourceInfoVnfcCpInfo vnfcResourceInfoVnfcCpInfo : vnfcResourceInfo.getVnfcCpInfo())
                                    if(vnfcResourceInfoVnfcCpInfo.getCpdId().equalsIgnoreCase(ids.get(2)))
                                        parameterValue = getIpAddress(vnfcResourceInfoVnfcCpInfo.getCpProtocolInfo());
                if(parameterValue == null)
                    throw new FailedOperationException("Unable to find the VDU Connection Point IP address");
                return parameterValue;
            case PNF_CP_IP_ADDRESS:
                log.debug("Finding IP address of Connection Point with ID {} for PNF with ID {}", ids.get(1), ids.get(0));
                for(PnfInfo pnfInfo : nsInstance.getPnfInfo()) {
                    if (pnfInfo.getPnfdId().equalsIgnoreCase(ids.get(0))) {
                        if (pnfInfo.getCpInfo().getCpdId().equalsIgnoreCase(ids.get(1))) {
                            List<CpProtocolData> cpProtocolData = pnfInfo.getCpInfo().getCpProtocolData();
                            IpOverEthernetAddressData ipOverEthernetAddressData = cpProtocolData.get(0).getIpOverEthernet();
                            for(IpOverEthernetAddressDataIpAddresses ipAddress : ipOverEthernetAddressData.getIpAddresses()){
                                if(ipAddress.getType().equals(IpOverEthernetAddressDataIpAddresses.TypeEnum.IPV4))
                                    parameterValue = ipAddress.getFixedAddresses().get(0);//TODO getFixedAddresses is okay?
                            }
                        }
                    }
                }
                if(parameterValue == null)
                    throw new FailedOperationException("Unable to find the PNF Connection Point IP address");
                return parameterValue;
            default: {
                log.error("Unsupported configuration parameter");
                throw new FailedOperationException("Unsupported configuration parameter");
            }
        }
    }

    private String getIpAddress(List<CpProtocolInfo> cpProtocolInfos){
        IpOverEthernetAddressInfo ipOverEthernetAddressInfo = cpProtocolInfos.get(0).getIpOverEthernet();//TODO get(0) is okay?
        for(IpOverEthernetAddressInfoIpAddresses ipAddress : ipOverEthernetAddressInfo.getIpAddresses()){
            if(ipAddress.getType().equals(IpOverEthernetAddressInfoIpAddresses.TypeEnum.IPV4))
                return ipAddress.getAddresses().get(0);//TODO get(0) is okay?
        }
        return null;
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
                return currentState.equals(ExperimentState.VALIDATING) ||
                        currentState.equals(ExperimentState.CONFIGURING) ||
                        currentState.equals(ExperimentState.RUNNING) ||
                        currentState.equals(ExperimentState.RUNNING_STEP);
            case ABORTED:
                return currentState.equals(ExperimentState.ABORTING);
            case RUNNING:
                return currentState.equals(ExperimentState.VALIDATING) ||
                        currentState.equals(ExperimentState.CONFIGURING) ||
                        (currentState.equals(ExperimentState.PAUSED) && runType.equals(ExperimentRunType.RUN_ALL));
            case ABORTING:
                return currentState.equals(ExperimentState.RUNNING) ||
                        currentState.equals(ExperimentState.RUNNING_STEP) ||
                        currentState.equals(ExperimentState.PAUSED) ||
                        currentState.equals(ExperimentState.CONFIGURING);
            case COMPLETED:
                return currentState.equals(ExperimentState.VALIDATING);
            case VALIDATING:
                return currentState.equals(ExperimentState.RUNNING) ||
                        currentState.equals(ExperimentState.RUNNING_STEP);
            case CONFIGURING:
                return currentState.equals(ExperimentState.INIT) ||
                        currentState.equals(ExperimentState.PAUSED) ||
                        currentState.equals(ExperimentState.VALIDATING) ||
                        currentState.equals(ExperimentState.RUNNING) ||
                        currentState.equals(ExperimentState.RUNNING_STEP);
            case RUNNING_STEP:
                return currentState.equals(ExperimentState.PAUSED);
            default:
                log.debug("New state not recognized");
                return false;
        }
    }
}
