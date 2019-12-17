package it.nextworks.eem.sbi.jenkins;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.nextworks.eem.configuration.ConfigurationParameters;
import it.nextworks.eem.rabbitMessage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class JenkinsService {

    private static final Logger log = LoggerFactory.getLogger(JenkinsService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier(ConfigurationParameters.eemQueueExchange)
    private TopicExchange messageExchange;

    public void runTestCase(String executionId, String tcDescriptorId, String robotFile){//TODO change type of robotFile
        new Thread(() -> {
            jenkinsStuff(executionId, tcDescriptorId, robotFile);
        }).start();
    }

    private void jenkinsStuff(String executionId, String tcDescriptorId, String robotFile){//TODO modify name
        //TODO run test case and get result
        try {//TODO remove
            log.debug("Running the experiment");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.debug("Sleep error");
        }
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
