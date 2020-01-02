package it.nextworks.eem.sbi.runtimeConfigurator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.nextworks.eem.configuration.ConfigurationParameters;
import it.nextworks.eem.rabbitMessage.AbortingResultInternalMessage;
import it.nextworks.eem.rabbitMessage.ConfigurationResultInternalMessage;
import it.nextworks.eem.rabbitMessage.InternalMessage;
import it.nextworks.eem.rabbitMessage.TestCaseResultInternalMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        value="eem.sbi.service.jenkins",
        havingValue = "false")
public class RunTimeConfiguratorService {
    private static final Logger log = LoggerFactory.getLogger(RunTimeConfiguratorService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier(ConfigurationParameters.eemQueueExchange)
    private TopicExchange messageExchange;

    public void configureExperiment(String executionId){
        new Thread(() -> {
            configurationStuff(executionId);
        }).start();
    }

    public void runTestCase(String executionId, String tcDescriptorId, String testCaseFile){
        new Thread(() -> {
            runningStuff(executionId, tcDescriptorId, testCaseFile);
        }).start();
    }

    public void abortTestCase(String executionId, String tcDescriptorId){
        new Thread(() -> {
            abortStuff(executionId, tcDescriptorId);
        }).start();
    }

    private void configurationStuff(String executionId){//TODO modify name
        //TODO configure the experiment
        try {//TODO remove
            log.debug("Configuring the experiment");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.debug("Sleep error");
        }
        //TODO remove, handled via Notification Endpoint
        //configuration ok
        String result = "OK";
        String topic = "lifecycle.configurationResult." + executionId;
        InternalMessage internalMessage = new ConfigurationResultInternalMessage(result, false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageConfigurationError("Error while translating internal scheduling message in Json format", executionId);
        }
        //configuration ko
        //manageConfigurationError();
    }

    private void runningStuff(String executionId, String tcDescriptorId, String testCaseFile){//TODO modify name
        //TODO run test case and get result
        try {//TODO remove
            log.debug("Running the experiment");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.debug("Sleep error");
        }
        //TODO remove, handled via Notification Endpoint
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
        //test ko
        //manageTestCaseError();
    }

    private void abortStuff(String executionId, String tcDescriptorId){
        //TODO abort test case
        try {//TODO remove
            log.debug("Aborting the experiment");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.debug("Sleep error");
        }
        //TODO remove, handled via Notification Endpoint
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
        //aborting ko
        //manageAbortingError();
    }

    private void manageConfigurationError(String errorMessage, String executionId){
        log.error("Configuration of Experiment Execution with Id {} failed : {}", executionId, errorMessage);
        errorMessage = String.format("Configuration of Experiment Execution with Id %s failed : %s", executionId, errorMessage);
        String topic = "lifecycle.configurationResult." + executionId;
        InternalMessage internalMessage = new ConfigurationResultInternalMessage(errorMessage, true);
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
