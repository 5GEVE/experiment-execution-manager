package it.nextworks.eem.sbi.runtimeConfigurator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.nextworks.eem.configuration.ConfigurationParameters;
import it.nextworks.eem.model.ConfigurationStatus;
import it.nextworks.eem.model.MetricInfo;
import it.nextworks.eem.rabbitMessage.AbortingResultInternalMessage;
import it.nextworks.eem.rabbitMessage.ConfigurationResultInternalMessage;
import it.nextworks.eem.rabbitMessage.InternalMessage;
import it.nextworks.eem.rabbitMessage.TestCaseResultInternalMessage;
import it.nextworks.eem.sbi.interfaces.ConfiguratorServiceProviderInterface;
import it.nextworks.eem.sbi.interfaces.ExecutorServiceProviderInterface;
import it.nextworks.eem.sbi.rav.RAVDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

public class RCDriver implements ConfiguratorServiceProviderInterface, ExecutorServiceProviderInterface {

    // static variable single_instance of type RCDriver
    private static RCDriver single_instance = null;

    private static final Logger log = LoggerFactory.getLogger(RCDriver.class);

    private RabbitTemplate rabbitTemplate;
    private TopicExchange messageExchange;

    // private constructor restricted to this class itself
    private RCDriver(RabbitTemplate rabbitTemplate, TopicExchange messageExchange) {
        log.debug("Initializing RC Driver : uri {}", "RC_URI");
        this.rabbitTemplate = rabbitTemplate;
        this.messageExchange = messageExchange;
    }

    // static method to create instance of RCDriver class
    public static RCDriver getInstance(RabbitTemplate rabbitTemplate, TopicExchange messageExchange) {
        if (single_instance == null)
            single_instance = new RCDriver(rabbitTemplate, messageExchange);
        else
            log.debug("RC Driver already instantiated: uri {}", "RC_URI");
        return single_instance;
    }

    @Override
    public void applyConfiguration(String executionId, String tcDescriptorId, String configScript){
        new Thread(() -> {
            applyConfigurationImplementation(executionId, tcDescriptorId, configScript);
        }).start();
    }

    @Override
    public void abortConfiguration(String executionId, String tcDescriptorId){
        new Thread(() -> {
            abortConfigurationImplementation(executionId, tcDescriptorId);
        }).start();
    }

    @Override
    public void configureInfrastructureMetricCollection(String executionId, String tcDescriptorId, List<MetricInfo> metrics){
        new Thread(() -> {
            configureInfrastructureMetricCollectionImplementation(executionId, tcDescriptorId, metrics);
        }).start();
    }

    @Override
    public void resetConfiguration(String executionId, String tcDescriptorId, String resetScript){
        new Thread(() -> {
            resetConfigurationImplementation(executionId, tcDescriptorId, resetScript);
        }).start();
    }

    @Override
    public void removeInfrastructureMetricCollection(String executionId, String tcDescriptorId, List<String> metricConfigIds){
        new Thread(() -> {
            removeInfrastructureMetricCollectionImplementation(executionId, tcDescriptorId, metricConfigIds);
        }).start();
    }

    @Override
    public void runTestCase(String executionId, String tcDescriptorId, String testCaseFile){
        new Thread(() -> {
            runTestCaseImplementation(executionId, tcDescriptorId, testCaseFile);
        }).start();
    }

    @Override
    public void abortTestCase(String executionId, String tcDescriptorId){
        new Thread(() -> {
            abortTestCaseImplementation(executionId, tcDescriptorId);
        }).start();
    }

    private void applyConfigurationImplementation(String executionId, String tcDescriptorId, String configScript){
        //TODO remove
        try {
            log.debug("Configuring the experiment");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.debug("Sleep error");
        }

        //TODO configure experiment
        //configuration ok
        String result = "OK";
        String topic = "lifecycle.configurationResult." + executionId;
        InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.CONFIGURED, result, null,false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageConfigurationError("Error while translating internal scheduling message in Json format", executionId);
        }

        //TODO handle configuration error
        //configuration ko
        //manageConfigurationError();
    }

    private void abortConfigurationImplementation(String executionId, String tcDescriptorId){
        //TODO abort configuration
        //no response message needed
    }

    private void configureInfrastructureMetricCollectionImplementation(String executionId, String tcDescriptorId, List<MetricInfo> metricst){
        //TODO remove
        try {
            log.debug("Configuring infrastructure metrics");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.debug("Sleep error");
        }

        //TODO configure infrastructure metrics
        //metric configuration ok
        String result = "OK";
        List<String> metricConfigIds = new ArrayList<>();
        metricConfigIds.add("metric1_id");//TODO use IDs returned by RC
        String topic = "lifecycle.configurationResult." + executionId;
        InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.METRIC_CONFIGURED, result, metricConfigIds,false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageConfigurationError("Error while translating internal scheduling message in Json format", executionId);
        }

        //TODO handle metric configuration error
        //metric configuration ko
        //manageConfigurationError();
    }

    private void resetConfigurationImplementation(String executionId, String tcDescriptorId, String resetScript){
        //TODO reset configuration
        //no response message needed
    }

    private void removeInfrastructureMetricCollectionImplementation(String executionId, String tcDescriptorId, List<String> metricConfigIds){
        //TODO remove infrastructure metrics
        //no response message needed
    }

    private void runTestCaseImplementation(String executionId, String tcDescriptorId, String testCaseFile){
        //TODO remove
        try {
            log.debug("Running the experiment");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.debug("Sleep error");
        }

        //TODO run test case and get result
        //test ok
        String result = "OK";
        String topic = "lifecycle.testCaseResult." + executionId;
        InternalMessage internalMessage = new TestCaseResultInternalMessage(result, tcDescriptorId, false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageTestCaseError("Error while translating internal scheduling message in Json format", executionId, tcDescriptorId);
        }

        //TODO handle run test case error
        //test ko
        //manageTestCaseError();
    }

    private void abortTestCaseImplementation(String executionId, String tcDescriptorId){
        //TODO remove
        try {
            log.debug("Aborting the experiment");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.debug("Sleep error");
        }

        //TODO abort test case
        //aborting ok
        String result = "OK";
        String topic = "lifecycle.abortingResult." + executionId;
        InternalMessage internalMessage = new AbortingResultInternalMessage(result, false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageAbortingError("Error while translating internal scheduling message in Json format", executionId);
        }

        //TODO handle aborting error
        //aborting ko
        //manageAbortingError();
    }

    private void manageConfigurationError(String errorMessage, String executionId){
        log.error("Configuration of Experiment Execution with Id {} failed : {}", executionId, errorMessage);
        errorMessage = String.format("Configuration of Experiment Execution with Id %s failed : %s", executionId, errorMessage);
        String topic = "lifecycle.configurationResult." + executionId;
        InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.FAILED, errorMessage, null, true);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            log.debug(null, e);
        }
    }

    private void manageTestCaseError(String errorMessage, String executionId, String tcDescriptorId){
        log.error("Test Case with Id {} of Experiment Execution with Id {} failed : {}", tcDescriptorId, executionId, errorMessage);
        errorMessage = String.format("Test Case with Id %s for Experiment Execution with Id %s failed : %s", tcDescriptorId, executionId, errorMessage);
        String topic = "lifecycle.testCaseResult." + executionId;
        InternalMessage internalMessage = new TestCaseResultInternalMessage(errorMessage, tcDescriptorId, true);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            log.debug(null, e);
        }
    }

    private void manageAbortingError(String errorMessage, String executionId){
        log.error("Aborting of Experiment Execution with Id {} failed : {}", executionId, errorMessage);
        errorMessage = String.format("Aborting of Experiment Execution with Id %s failed : %s", executionId, errorMessage);
        String topic = "lifecycle.abortingResult." + executionId;
        InternalMessage internalMessage = new AbortingResultInternalMessage(errorMessage, true);
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
