package it.nextworks.eem.sbi.dummyDrivers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.nextworks.eem.rabbitMessage.InternalMessage;
import it.nextworks.eem.rabbitMessage.ValidationResultInternalMessage;
import it.nextworks.eem.sbi.interfaces.ValidatorServiceProviderInterface;
import it.nextworks.eem.model.ValidationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class DummyValidatorDriver implements ValidatorServiceProviderInterface {

    private static final Logger log = LoggerFactory.getLogger(DummyValidatorDriver.class);

    private RabbitTemplate rabbitTemplate;
    private TopicExchange messageExchange;

    public DummyValidatorDriver(RabbitTemplate rabbitTemplate, TopicExchange messageExchange) {
        log.debug("Initializing Dummy Validator Driver");
        this.rabbitTemplate = rabbitTemplate;
        this.messageExchange = messageExchange;

    }

    @Override
    public void configureExperiment(String experimentId, String executionId){
        log.debug("ConfiguringExperiment request: {}", executionId);
        String topic = "lifecycle.validation." + executionId;
        InternalMessage internalMessage = new ValidationResultInternalMessage(ValidationStatus.CONFIGURED, "Validation done by DUMMY", false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageValidationError("Error while translating internal scheduling message in Json format", executionId);
        }
    }

    @Override
    public void startTcValidation(String experimentId, String executionId, String tcDescriptorId){
        log.debug("Started test case validation for execution {} and test case descriptor {}", executionId, tcDescriptorId);
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

    @Override
    public void stopTcValidation(String experimentId, String executionId, String tcDescriptorId){
        log.debug("Requested to stop test case validation for experiment {} and test case descriptor {}", executionId, tcDescriptorId);
        String validationStarted = "OK";
        String topic = "lifecycle.validation." + executionId;
        InternalMessage internalMessage = new ValidationResultInternalMessage(ValidationStatus.VALIDATING, validationStarted, false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageValidationError("Error while translating internal scheduling message in Json format", executionId);
        }
    }

    @Override
    public void queryValidationResult(String experimentId, String executionId, String tcDescriptorId){
        log.debug("Query validation result on execution {} and test case descriptor {}", executionId, tcDescriptorId);
        String reportUrl = "http://dummy.url" + executionId + "/index.html";
        String topic = "lifecycle.validation." + executionId;
        InternalMessage internalMessage = new ValidationResultInternalMessage(ValidationStatus.VALIDATED, reportUrl, false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageValidationError("Error while translating internal scheduling message in Json format", executionId);
        }
    }

    @Override
    public void terminateExperiment(String experimentId, String executionId){
        log.debug("Termination request for execution {}", executionId);
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
}
