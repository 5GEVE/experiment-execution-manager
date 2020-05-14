package it.nextworks.eem.sbi.dummyDrivers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.nextworks.eem.rabbitMessage.AbortingResultInternalMessage;
import it.nextworks.eem.rabbitMessage.InternalMessage;
import it.nextworks.eem.rabbitMessage.TestCaseResultInternalMessage;
import it.nextworks.eem.sbi.interfaces.ExecutorServiceProviderInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class DummyExecutorDriver implements ExecutorServiceProviderInterface {

    private static final Logger log = LoggerFactory.getLogger(DummyExecutorDriver.class);

    private RabbitTemplate rabbitTemplate;
    private TopicExchange messageExchange;
    public DummyExecutorDriver(RabbitTemplate rabbitTemplate, TopicExchange messageExchange) {
        log.debug("Initializing Dummy Executor Driver");
        this.rabbitTemplate = rabbitTemplate;
        this.messageExchange = messageExchange;
    }

    @Override
    public void runTestCase(String executionId, String tcDescriptorId, String executionScript){
        manageTestCaseOK("OK", executionId, tcDescriptorId);
    }

    @Override
    public void abortTestCase(String executionId, String tcDescriptorId){
        String result = "OK";
        String topic = "lifecycle.abortingResult." + executionId;
        InternalMessage internalMessage = new AbortingResultInternalMessage(result, false);
        try {
            Thread.sleep(10000);
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageAbortingError("Error while translating internal scheduling message in Json format", executionId);
        } catch (InterruptedException e2){
            log.error("Error while sleeping {}", e2.getMessage());
        }
    }

    private void sendMessageToQueue(InternalMessage msg, String topic) throws JsonProcessingException {
        ObjectMapper mapper = buildObjectMapper();
        String json = mapper.writeValueAsString(msg);
        rabbitTemplate.convertAndSend(messageExchange.getName(), topic, json);
    }

    private void manageTestCaseOK(String result, String executionId, String tcDescriptorId){
        String topic = "lifecycle.testCaseResult." + executionId;

        InternalMessage internalMessage = new TestCaseResultInternalMessage(result, tcDescriptorId, false);
        try {
            Thread.sleep(10000);
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format {}", e.getMessage());
        } catch (InterruptedException e2){
            log.error("Error while sleeping {}", e2.getMessage());
        }
    }
    private ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
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

}
