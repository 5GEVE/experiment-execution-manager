package it.nextworks.eem.sbi.jenkins;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.nextworks.eem.rabbitMessage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;

public class SbiJenkinsService {

    private static final Logger log = LoggerFactory.getLogger(SbiJenkinsService.class);

    private String executionId;
    private String currentTestCaseId;
    private RabbitTemplate rabbitTemplate;
    private TopicExchange messageExchange;

    public SbiJenkinsService(String executionId, RabbitTemplate rabbitTemplate, TopicExchange messageExchange){
        this.executionId = executionId;
        this.rabbitTemplate = rabbitTemplate;
        this.messageExchange = messageExchange;
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
                case RUN_TEST_CASE: {
                    RunTestCaseInternalMessage msg = (RunTestCaseInternalMessage) im;
                    this.currentTestCaseId = msg.getTcDescriptorId();
                    log.debug("Processing request to run Test Case with Id {} for Experiment Execution with Id {}", currentTestCaseId, executionId);
                    runTestCase(msg);
                    break;
                }
                default:
                    log.error("Received message with not supported type. Skipping.");
                    break;
            }
        } catch (JsonParseException e) {
            log.debug(null, e);
            manageTestCaseError("Error while parsing message: " + e.getMessage());
        } catch (JsonMappingException e) {
            log.debug(null, e);
            manageTestCaseError("Error in Json mapping: " + e.getMessage());
        } catch (IOException e) {
            log.debug(null, e);
            manageTestCaseError("IO error when receiving json message: " + e.getMessage());
        }
    }

    private void runTestCase(RunTestCaseInternalMessage msg){
        //TODO run test case and get result
        String result = "OK";
        String topic = "lifecycle.testCaseResult." + executionId;
        InternalMessage internalMessage = new TestCaseResultInternalMessage(result, currentTestCaseId, false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageTestCaseError("Error while translating internal scheduling message in Json format");
        } //TODO handle FailedOperationException
    }

    private void manageTestCaseError(String errorMessage){
        log.error("Test Case with Id {} for Experiment Execution with Id {} failed : {}", currentTestCaseId, executionId, errorMessage);
        errorMessage = String.format("Test Case with Id %s for Experiment Execution with Id %s failed : %s", currentTestCaseId, executionId, errorMessage);
        String topic = "lifecycle.testCaseResult." + executionId;
        InternalMessage internalMessage = new TestCaseResultInternalMessage(errorMessage, currentTestCaseId, true);
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
