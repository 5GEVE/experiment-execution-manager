package it.nextworks.eem.sbi.runtimeConfigurator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.nextworks.eem.configuration.ConfigurationParameters;
import it.nextworks.eem.rabbitMessage.ConfigurationResultInternalMessage;
import it.nextworks.eem.rabbitMessage.InternalMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier(ConfigurationParameters.eemQueueExchange)
    private TopicExchange messageExchange;

    public void configureExperiment(String executionId, String runType){
        new Thread(() -> {
            configurationStuff(executionId, runType);
        }).start();
    }

    private void configurationStuff(String executionId, String runType){//TODO modify name
        //TODO run test case and get result
        try {//TODO remove
            log.debug("Configuring the experiment");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.debug("Sleep error");
        }
        //test ok
        String result = "OK";
        String topic = "lifecycle.configuration." + executionId;
        InternalMessage internalMessage = new ConfigurationResultInternalMessage(result, runType, false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageConfigurationError("Error while translating internal scheduling message in Json format", executionId, runType);
        }
        //test ko
        //manageConfigurationError();
    }

    private void manageConfigurationError(String errorMessage, String executionId, String runType){
        log.error("Configuration of Experiment Execution with Id {} failed : {}", executionId, errorMessage);
        errorMessage = String.format("Configuration of Experiment Execution with Id %s failed : %s", executionId, errorMessage);
        String topic = "lifecycle.configuration." + executionId;
        InternalMessage internalMessage = new ConfigurationResultInternalMessage(errorMessage, runType, true);
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
