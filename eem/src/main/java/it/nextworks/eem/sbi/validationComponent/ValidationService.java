package it.nextworks.eem.sbi.validationComponent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.nextworks.eem.configuration.ConfigurationParameters;
import it.nextworks.eem.rabbitMessage.InternalMessage;
import it.nextworks.eem.rabbitMessage.ValidationResultInternalMessage;
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
public class ValidationService {

    private static final Logger log = LoggerFactory.getLogger(ValidationService.class);

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

    public void startTcValidation(String executionId, String tcDescriptorId){
        new Thread(() -> {
            startValidationStuff(executionId, tcDescriptorId);
        }).start();
    }

    public void stopTcValidation(String executionId, String tcDescriptorId){
        new Thread(() -> {
            stopValidationStuff(executionId, tcDescriptorId);
        }).start();
    }

    public void queryValidationResult(String executionId, String tcDescriptorId){
        new Thread(() -> {
            queryValidationResultStuff(executionId, tcDescriptorId);
        }).start();
    }

    public void terminateExperiment(String executionId){
        new Thread(() -> {
            terminationStuff(executionId);
        }).start();
    }

    private void startValidationStuff(String executionId, String tcDescriptorId){//TODO modify name
        //TODO start TC validation
        try {//TODO remove
            log.debug("Starting the tc validation");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.debug("Sleep error");
        }

        //TODO remove, handled via Notification Endpoint
        //validation started
        String validationStarted = "OK";
        String topic = "lifecycle.validation." + executionId;
        InternalMessage internalMessage = new ValidationResultInternalMessage(ValidationStatus.ACQUIRING, validationStarted, false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageValidationError("Error while translating internal scheduling message in Json format", executionId);
        }
        //validation not started
        //manageValidationError();
    }

    private void stopValidationStuff(String executionId, String tcDescriptorId){//TODO modify name
        //TODO stop TC validation
        try {//TODO remove
            log.debug("Stopping the tc validation");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.debug("Sleep error");
        }
        //TODO remove, handled via Notification Endpoint
        //validation stopped
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

    private void queryValidationResultStuff(String executionId, String tcDescriptorId){//TODO modify name
        //TODO query TC validation
        try {//TODO remove
            log.debug("Querying tc validation");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.debug("Sleep error");
        }
        //TODO insert some delay in performing queries..Every time EEIM receives a VALIDATING message, it sends immediately a new queryValidationResult

        //TODO remove, handled via Notification Endpoint
        ValidationStatus validationStatus;
        String reportUrl = null;
        String topic = "lifecycle.validation." + executionId;
        //if VALIDATING
        if(false)
            validationStatus = ValidationStatus.VALIDATING;
        else{
            validationStatus = ValidationStatus.VALIDATED;
            reportUrl = "reportUrl";
        }
        InternalMessage internalMessage = new ValidationResultInternalMessage(validationStatus, reportUrl, false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageValidationError("Error while translating internal scheduling message in Json format", executionId);
        }
        //cannot query validation stuff
        //manageValidationError();
    }

    private void configurationStuff(String executionId){//TODO modify name
        //TODO configure experiment validation
        try {//TODO remove
            log.debug("Configuring the experiment validation");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.debug("Sleep error");
        }

        //TODO remove, handled via Notification Endpoint
        //configuration ok
        String configuration = "OK";
        String topic = "lifecycle.validation." + executionId;
        InternalMessage internalMessage = new ValidationResultInternalMessage(ValidationStatus.CONFIGURED, "Validation ok", false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageValidationError("Error while translating internal scheduling message in Json format", executionId);
        }
        //configuration ko
        //manageValidationError();
    }

    private void terminationStuff(String executionId){//TODO modify name
        //TODO terminate and remove experiment
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
