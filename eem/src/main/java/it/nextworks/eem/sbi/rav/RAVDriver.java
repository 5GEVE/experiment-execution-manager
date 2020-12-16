package it.nextworks.eem.sbi.rav;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;
import it.nextworks.eem.model.ExperimentExecution;
import it.nextworks.eem.model.ValidationStatus;
import it.nextworks.eem.rabbitMessage.InternalMessage;
import it.nextworks.eem.rabbitMessage.ValidationResultInternalMessage;
import it.nextworks.eem.repo.ExperimentExecutionRepository;
import it.nextworks.eem.sbi.expcatalogue.ExperimentCatalogueService;
import it.nextworks.eem.sbi.interfaces.ValidatorServiceProviderInterface;
import it.nextworks.eem.sbi.rav.api.ApiClient;
import it.nextworks.eem.sbi.rav.api.ValidationApi;
import it.nextworks.eem.sbi.rav.model.*;
import it.nextworks.nfvmano.catalogue.blueprint.elements.*;
import it.nextworks.nfvmano.catalogue.blueprint.messages.*;
import it.nextworks.nfvmano.libs.ifa.common.elements.Filter;
import it.nextworks.nfvmano.libs.ifa.common.messages.GeneralizedQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.*;

public class RAVDriver implements ValidatorServiceProviderInterface {

    // static variable single_instance of type RAVDriver
    private static RAVDriver single_instance = null;

    private static final Logger log = LoggerFactory.getLogger(RAVDriver.class);

    private RabbitTemplate rabbitTemplate;
    private TopicExchange messageExchange;

    private ExperimentExecutionRepository experimentExecutionRepository;
    private ExperimentCatalogueService catalogueService;

    private ValidationApi ravApi;
    private String monitoringAddress;
    private String monitoringPort;

    // private constructor restricted to this class itself
    private RAVDriver(String ravURI, String monitoringAddress, String monitoringPort, ExperimentCatalogueService catalogueService, ExperimentExecutionRepository experimentExecutionRepository, RabbitTemplate rabbitTemplate, TopicExchange messageExchange) {
        log.debug("Initializing RAV Driver : uri {}", ravURI);
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(ravURI);
        ravApi = new ValidationApi(apiClient);
        this.monitoringAddress = monitoringAddress;
        this.monitoringPort = monitoringPort;
        this.catalogueService = catalogueService;
        this.experimentExecutionRepository = experimentExecutionRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.messageExchange = messageExchange;
    }

    // static method to create instance of RAVDriver class
    public static RAVDriver getInstance(String ravURI, String monitoringAddress, String monitoringPort, ExperimentCatalogueService catalogueService, ExperimentExecutionRepository experimentExecutionRepository, RabbitTemplate rabbitTemplate, TopicExchange messageExchange) {
        if (single_instance == null)
            single_instance = new RAVDriver(ravURI, monitoringAddress, monitoringPort, catalogueService, experimentExecutionRepository, rabbitTemplate, messageExchange);
        else
            log.debug("RAV Driver already instantiated: uri {}", ravURI);
        return single_instance;
    }

    @Override
    public void configureExperiment(String experimentId, String executionId){
        new Thread(() -> {
            configurationStuff(experimentId, executionId);
        }).start();
    }

    @Override
    public void startTcValidation(String experimentId, String executionId, String tcDescriptorId){
        new Thread(() -> {
            startValidationStuff(experimentId, executionId, tcDescriptorId);
        }).start();
    }

    @Override
    public void stopTcValidation(String experimentId, String executionId, String tcDescriptorId){
        new Thread(() -> {
            stopValidationStuff(experimentId, executionId, tcDescriptorId);
        }).start();
    }

    @Override
    public void queryValidationResult(String experimentId, String executionId, String tcDescriptorId){
        new Thread(() -> {
            queryValidationResultStuff(experimentId, executionId, tcDescriptorId);
        }).start();
    }

    @Override
    public void terminateExperiment(String experimentId, String executionId){
        new Thread(() -> {
            terminationStuff(experimentId, executionId);
        }).start();
    }

    private void startValidationStuff(String experimentId, String executionId, String tcDescriptorId){//TODO modify name
        log.info("Starting new validation task for execution {} and test case {}", executionId, tcDescriptorId);

        try {
            Call call = ravApi.startTestcaseValidationCall(executionId, tcDescriptorId, null, null);
            Response response = call.execute();
            if (response.code() != 200){
                log.error("Status code {} on start validation of execution {} and tcDescriptor {}", response.code(), executionId, tcDescriptorId);
                manageValidationError("Status code "+ response.code() +" on start validation of execution " + executionId + " and tcDescriptor " + tcDescriptorId, executionId);
                return;
            }
        } catch (Exception e1) {
            log.error("Exception on start validation of execution {} and tcDescriptor {}", executionId, tcDescriptorId);
            e1.getMessage();
            manageValidationError("Exception on start validation of execution " + executionId + " and tcDescriptor " + tcDescriptorId, executionId);
            return;
        }

        String validationStarted = "OK";
        String topic = "lifecycle.validation." + executionId;
        InternalMessage internalMessage = new ValidationResultInternalMessage(ValidationStatus.ACQUIRING, validationStarted, false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageValidationError("Error while translating internal scheduling message in Json format", executionId);
        }
    }

    private void stopValidationStuff(String experimentId, String executionId, String tcDescriptorId){//TODO modify name
        //TODO stop TC validation
        try {//TODO remove
            Call call = ravApi.terminateCurrentTestcaseCall(executionId, tcDescriptorId, null, null);
            Response response = call.execute();
            if (response.code() != 200){
                log.error("Status code {} on stop validation of execution {} and tcDescriptor {}", response.code(), executionId, tcDescriptorId);
                manageValidationError("Status code "+ response.code() +" on stop validation of execution " + executionId + " and tcDescriptor " + tcDescriptorId, executionId);
                return;
            }
        } catch (Exception e) {
            log.error("Exception on stop validation of execution {} and tcDescriptor {}", executionId, tcDescriptorId);
            e.getMessage();
            manageValidationError("Exception on stop validation of execution " + executionId + " and tcDescriptor " + tcDescriptorId, executionId);
            return;
        }

        String validationStarted = "OK";
        String topic = "lifecycle.validation." + executionId;
        InternalMessage internalMessage = new ValidationResultInternalMessage(ValidationStatus.VALIDATING, validationStarted, false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageValidationError("Error while translating internal scheduling message in Json format", executionId);
        }
        //validation not stopped
        //manageValidationError();
    }

    private void queryValidationResultStuff(String experimentId, String executionId, String tcDescriptorId){//TODO modify name
          StatusResponse statusResponse = null;
//        try {
//            Call call = ravApi.startTestcaseValidationCall(executionId, tcDescriptorId, null, null);
//            Response response = call.execute();
//            if (response.code() != 200){
//                log.error("Failed to start validation of execution {} and tcDiD {}: Response code  received {}", executionId, tcDescriptorId, response.code());
//                manageValidationError("Failed to start validation of execution " + executionId + " and tcDiD " + tcDescriptorId + ": Response code received " + response.code() , executionId);
//            }
//        } catch(Exception e1){
//            log.error("Failed to start validation of execution {} and tcDiD {}", executionId, tcDescriptorId);
//            e1.getMessage();
//            manageValidationError("Error while translating internal scheduling message in Json format", executionId);
//        }

            try {
                Thread.sleep(5000);
                statusResponse = ravApi.showTestcaseValidationStatus(executionId, tcDescriptorId);
            } catch (Exception e2){
                log.error("Error trying to validate execution {} and tcDescriptor {}", executionId, tcDescriptorId);
                manageValidationError("Error trying to validate execution " + executionId + " and tcDescriptor " + tcDescriptorId, executionId);
                return;
            }

        //TODO insert some delay in performing queries..Every time EEM receives a VALIDATING message, it sends immediately a new queryValidationResult

        //TODO remove, handled via Notification Endpoint
        ValidationStatus validationStatus;
        String reportUrl = null;
        String topic = "lifecycle.validation." + executionId;
        //if VALIDATING
        if(! statusResponse.getStatus().equalsIgnoreCase("VALIDATED"))
            validationStatus = ValidationStatus.VALIDATING;
        else{
            validationStatus = ValidationStatus.VALIDATED;
            reportUrl = statusResponse.getReport();
        }
        InternalMessage internalMessage = new ValidationResultInternalMessage(validationStatus, reportUrl, false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageValidationError("Error while translating internal scheduling message in Json format", executionId);
        }

    }

    private void configurationStuff(String experimentId, String executionId){
        //TODO configure experiment validation
        // Get experimentDescriptorId from experiment execution id
        Optional<ExperimentExecution> expExecutionInstance = experimentExecutionRepository.findByExecutionId(executionId);
        if (! expExecutionInstance.isPresent()) {
            log.error("Experiment execution with id {} not found", executionId);
            manageValidationError("Experiment execution with id {} not found", executionId);
            return;
        }
        ExperimentExecution expExecution = expExecutionInstance.get();

        // GET from catalogue the ExpD
        Map<String, String> parameters = new HashMap<>();
        Filter filter = new Filter(parameters);
        GeneralizedQueryRequest request = new GeneralizedQueryRequest(filter, null);
        //Retrieve Experiment Descriptor
        log.debug("Going to retrieve Experiment Descriptor with Id {}", expExecution.getExperimentDescriptorId());
        parameters.put("ExpD_ID", expExecution.getExperimentDescriptorId());
        QueryExpDescriptorResponse expDescriptorResponse = null;
        try{
            expDescriptorResponse = catalogueService.queryExpDescriptor(request);
        } catch (Exception e1){
            log.error("Unable to retrieve Experiment Descriptor identified with {} from the catalogue", expExecution.getExperimentDescriptorId());
            manageValidationError("Unable to retrieve Experiment Descriptor from the catalogue", executionId);
            return;
        }
        if (expDescriptorResponse.getExpDescriptors().size() != 1){
            log.error("List should contain a single experiment descriptor with id {}", expExecution.getExperimentDescriptorId());
            manageValidationError("List should contain a single experiment descriptor", executionId);
            return;
        }
        ExpDescriptor expDescriptor = expDescriptorResponse.getExpDescriptors().get(0);

        parameters.remove("ExpD_ID");
        log.debug("Going to retrieve Experiment Blueprint with Id {}", expDescriptor.getExpBlueprintId());
        parameters.put("ExpB_ID", expDescriptor.getExpBlueprintId());
        QueryExpBlueprintResponse expBlueprintResponse =  null;
        try {
            expBlueprintResponse = catalogueService.queryExpBlueprint(request);
        } catch (Exception e2) {
            log.error("Unable to retrieve Experiment blueprint identified with {} from the catalogue", expDescriptor.getExpBlueprintId());
            manageValidationError("Unable to retrieve Experiment blueprint from the catalogue", executionId);
            return;
        }
        ExpBlueprint expBlueprint = expBlueprintResponse.getExpBlueprintInfo().get(0).getExpBlueprint();
        parameters.remove("ExpB_ID");
        log.debug("Going to retrieve Context Blueprint");
        QueryCtxBlueprintResponse contextBlueprints = null;
        List<CtxBlueprint> ctxBlueprintList = new ArrayList<>();

        for (String ctxBId : expBlueprint.getCtxBlueprintIds()){
            parameters.put("CTXB_ID", ctxBId);
            try {
                contextBlueprints = catalogueService.queryCtxBlueprint(request);
                if(contextBlueprints.getCtxBlueprintInfos().size() != 1 ){
                    log.error("Catalogue should return single ctxB. Returned list size: {}", contextBlueprints.getCtxBlueprintInfos().size());
                    manageValidationError("Catalogue should return single ctxB", executionId);
                    return;
                } else {
                    ctxBlueprintList.add(contextBlueprints.getCtxBlueprintInfos().get(0).getCtxBlueprint());
                }
            } catch (Exception e3) {
                log.error("Unable to retrieve context blueprint identified with {} from the catalogue", ctxBId);
                manageValidationError("Unable to retrieve context blueprint from the catalogue", executionId);
                return;
            }
            parameters.remove("CTXB_ID");
        }
        log.debug("Retrieving VSBlueprint");
        parameters.put("VSB_ID", expBlueprint.getVsBlueprintId());//todo
        QueryVsBlueprintResponse vsBlueprintResponse = null;
        try{
            vsBlueprintResponse = catalogueService.queryVsBlueprint(request);
        } catch( Exception e4){
            log.error("Unable to retrieve Virtual Service Blueprint identified with {} from the catalogue", expBlueprint.getVsBlueprintId());
            manageValidationError("Unable to retrieve Virtual Service Blueprint from the catalogue", executionId);
            return;
        }
        VsBlueprint vsBlueprint = vsBlueprintResponse.getVsBlueprintInfo().get(0).getVsBlueprint();

        String siteName = expExecution.getSiteNames().get(0).toLowerCase();
        // TODO: Topic cannot be related to TCs. Will be sent all of them for each TC (should be changed interface on RAV)
        List<Topic> topics = new ArrayList<>();
        List<Publishtopic> publishTopics = new ArrayList<>();

        log.debug("Started generating application metrics");
        for (Map.Entry<String, String> entry: expExecution.getApplicationMetrics().entrySet()){
            Topic topic = new Topic();
            topic.brokerAddr(monitoringAddress+":"+monitoringPort);
            topic.setMetric(entry.getKey());
            topic.setTopic(entry.getValue());
            log.debug("topic name created for RAV: {}", entry.getValue());
            topics.add(topic);
        }
        log.debug("Stopped generating application metrics");

        log.debug("Started generating infrastructure metrics");
        for (Map.Entry<String, String> entry: expExecution.getInfrastructureMetrics().entrySet()){
            Topic topic = new Topic();
            topic.brokerAddr(monitoringAddress+":"+monitoringPort);
            topic.setMetric(entry.getKey());
            topic.setTopic(entry.getValue());
            log.debug("topic name created for RAV: {}", entry.getValue());
            topics.add(topic);
        }
        log.debug("Stopped generating infrastructure metrics");

        log.debug("Start generating application metrics");
        for (Map.Entry<String, String> entry: expExecution.getKpiMetrics().entrySet()){
            for (KeyPerformanceIndicator kpi : expBlueprintResponse.getExpBlueprintInfo().get(0).getExpBlueprint().getKpis()){
                if (entry.getKey().equalsIgnoreCase(kpi.getKpiId())){
                    Publishtopic pt = new Publishtopic();
                    pt.setBrokerAddr(monitoringAddress+":"+monitoringPort);
                    pt.setKpi(kpi.getKpiId());
                    pt.setFormula(kpi.getFormula());
                    pt.setInterval(new BigDecimal(kpi.getInterval().replaceAll("[^0-9.]+", ""))); //TODO check if converts it in a proper way
                    List<String> metricsIds = new ArrayList<>();
                    pt.setUnit(kpi.getUnit());
                    for (String metric : kpi.getMetricIds()){
                        metricsIds.add(metric);
                    }
                    KpiThreshold kpiThreshold = expDescriptor.getKpiThresholds().get(kpi.getKpiId());
                    pt.setUpperBound(String.valueOf(kpiThreshold.getUpperBound()));
                    pt.setLowerBound(String.valueOf(kpiThreshold.getLowerBound()));
                    pt.setInput(metricsIds);
                    pt.setTopic(entry.getValue());
                    log.debug("KPI topics created for RAV: {}", entry.getValue());
                    publishTopics.add(pt);
                }
            }
        }
        log.debug("Stopped generating kpi metrics");
        // Application metrics from Context blueprints
//        for (CtxBlueprint ctxB : ctxBlueprintList) {
//            log.debug("Adding application metrics for context: {}", ctxB.getName());
//            for (ApplicationMetric amd : ctxB.getApplicationMetrics()) {
//                Topic topic = new Topic();
//                topic.brokerAddr(monitoringAddress+":"+monitoringPort);
//                topic.setMetric(amd.getMetricId().toLowerCase());
//                topic.setTopic(expExecution.getUseCase()+"."+expExecution.getExperimentId()+"."+siteName+"."+MetricDataType.APPLICATION_METRIC.toString().toLowerCase()+"."+amd.getMetricId());
//                log.debug("topic name created for RAV: {}", expExecution.getUseCase()+"."+expExecution.getExperimentId()+"."+siteName+"."+MetricDataType.APPLICATION_METRIC.toString().toLowerCase()+"."+amd.getMetricId());
//                topics.add(topic);
//            }
//        }
        // Application metrics from VS Blueprint
//        log.debug("Adding application metrics for vsBlueprint: {}", vsBlueprint.getName());
//        for (ApplicationMetric amd : vsBlueprint.getApplicationMetrics()){
//            Topic topic = new Topic();
//            topic.brokerAddr(monitoringAddress+":"+monitoringPort);
//            topic.setMetric(amd.getMetricId().toLowerCase());
//            topic.setTopic(expExecution.getUseCase()+"."+expExecution.getExperimentId()+"."+siteName+"."+MetricDataType.APPLICATION_METRIC.toString().toLowerCase()+"."+amd.getMetricId());
//            log.debug("topic created for RAV: {}", expExecution.getUseCase()+"."+expExecution.getExperimentId()+"."+siteName+"."+MetricDataType.APPLICATION_METRIC.toString().toLowerCase()+"."+amd.getMetricId());
//            topics.add(topic);
//        }

        // Infrastructure metrics from Experiment blueprint
//        log.debug("Adding infrastructure metrics for expBlueprint {}", expBlueprintResponse.getExpBlueprintInfo().get(0).getName());
//        for (InfrastructureMetric im : expBlueprintResponse.getExpBlueprintInfo().get(0).getExpBlueprint().getMetrics()){
//            Topic topic = new Topic();
//            topic.brokerAddr(monitoringAddress+":"+monitoringPort);
//            topic.setMetric(im.getMetricId().toLowerCase());
//            topic.setTopic(expExecution.getUseCase()+"."+expExecution.getExperimentId()+"."+siteName+"."+MetricDataType.INFRASTRUCTURE_METRIC.toString().toLowerCase()+"."+im.getMetricId());
//            log.debug("topic created for RAV: {}", expExecution.getUseCase()+"."+expExecution.getExperimentId()+"."+siteName+"."+ MetricDataType.INFRASTRUCTURE_METRIC.toString().toLowerCase()+"."+im.getMetricId());
//            topics.add(topic);
//        }

        // KPIs
//        log.debug("Adding KPIs for expBlueprint {}", expBlueprintResponse.getExpBlueprintInfo().get(0).getName());
//        for (KeyPerformanceIndicator kpi : expBlueprintResponse.getExpBlueprintInfo().get(0).getExpBlueprint().getKpis()){
//            Publishtopic pt = new Publishtopic();
//            pt.setBrokerAddr(monitoringAddress+":"+monitoringPort);
//            pt.setKpi(kpi.getKpiId());
//            pt.setFormula(kpi.getFormula());
//            pt.setInterval(new BigDecimal(kpi.getInterval().replaceAll("[^0-9.]+", ""))); //TODO check if converts it in a proper way
//            List<String> metricsIds = new ArrayList<>();
//            pt.setUnit(kpi.getUnit());
//            for (String metric : kpi.getMetricIds()){
//                metricsIds.add(metric);
//            }
//            KpiThreshold kpiThreshold = expDescriptor.getKpiThresholds().get(kpi.getKpiId());
//            pt.setUpperBound(String.valueOf(kpiThreshold.getUpperBound()));
//            pt.setLowerBound(String.valueOf(kpiThreshold.getLowerBound()));
//            pt.setInput(metricsIds);
//            pt.setTopic(expExecution.getUseCase()+"."+expExecution.getExperimentId()+"."+siteName+"."+MetricDataType.KPI.toString().toLowerCase()+"."+kpi.getKpiId());
//            log.debug("KPI topics created for RAV: {}", expExecution.getUseCase()+"."+expExecution.getExperimentId()+"."+siteName+"."+ MetricDataType.KPI.toString().toLowerCase()+"."+kpi.getKpiId());
//            publishTopics.add(pt);
//        }

        ConfigurationDict confDict = new ConfigurationDict();
        confDict.setVertical(expExecution.getTenantId());
        confDict.setExpID(executionId);
//        confDict.setExpID(experimentId);
//        confDict.setExecutionID(executionId);
        List<ConfigurationDictTestcases> testCasesListConfig = new ArrayList<ConfigurationDictTestcases>();
        for (String tcdId : expDescriptor.getTestCaseDescriptorIds()){
            ConfigurationDictTestcases confDictTC = new ConfigurationDictTestcases();
            confDictTC.setTcID(tcdId);
            ModelConfiguration model = new ModelConfiguration();
            model.setPublish(publishTopics);
            model.setTopics(topics);
            confDictTC.setConfiguration(model);
            testCasesListConfig.add(confDictTC);
        }
        confDict.setTestcases(testCasesListConfig);

        try {
            Call call = ravApi.setConfigurationExpCall(confDict, executionId, null, null);
            Response response = call.execute();
            if (response.code() == 200) {
                String configuration = "OK";
                String topic = "lifecycle.validation." + executionId;
                InternalMessage internalMessage = new ValidationResultInternalMessage(ValidationStatus.CONFIGURED, "Validation ok", false);
                try {
                    sendMessageToQueue(internalMessage, topic);
                } catch (JsonProcessingException e) {
                    log.error("Error while translating internal scheduling message in Json format");
                    manageValidationError("Error while translating internal scheduling message in Json format", executionId);
                }
            } else {
                log.error("Configuration on RAV failed. Returned status {}", response.code());
                manageValidationError("Configuration on RAV failed", executionId);
            }
        } catch (Exception e5){
            log.error("Configuration action failed");
            manageValidationError("Configuration action failed", executionId);
        }

    }

    private void terminationStuff(String experimentId, String executionId){
        Call call = null;
        try{
            call = ravApi.terminateExperimentCall(executionId, null, null);
            Response response = call.execute();
            if (response.code() == 200) {
                log.debug("Validation terminated correctly");
            } else {
                log.error("RAV server replied with an error code {}", response.code());
            }
        } catch(Exception e){
            log.error("Something went wrong terminating experiment validation identified by {}", executionId);
        }
    }

    private void manageValidationError(String errorMessage, String executionId){
        log.error("Validation of Experiment Execution with Id {} failed : {}", executionId, errorMessage);
        errorMessage = String.format("Validation of Experiment Execution with Id %s failed : %s", executionId, errorMessage);
        String topic = "lifecycle.validation." + executionId;
        InternalMessage internalMessage = new ValidationResultInternalMessage(ValidationStatus.FAILED, errorMessage, true);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            log.debug(null, e);
        }
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
