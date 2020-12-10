package it.nextworks.eem.sbi.dummyDrivers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.nextworks.eem.model.ConfigurationStatus;
import it.nextworks.eem.model.MetricInfo;
import it.nextworks.eem.rabbitMessage.ConfigurationResultInternalMessage;
import it.nextworks.eem.rabbitMessage.InternalMessage;
import it.nextworks.eem.sbi.interfaces.ConfiguratorServiceProviderInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

public class DummyConfiguratorDriver implements ConfiguratorServiceProviderInterface {

    private static final Logger log = LoggerFactory.getLogger(DummyConfiguratorDriver.class);
    private RabbitTemplate rabbitTemplate;
    private TopicExchange messageExchange;

    public DummyConfiguratorDriver(RabbitTemplate rabbitTemplate, TopicExchange messageExchange) {
        log.debug("Initializing Dummy Configurator Driver");
        this.rabbitTemplate = rabbitTemplate;
        this.messageExchange = messageExchange;
    }


    @Override
    public void applyConfiguration(String executionId, String tcDescriptorId, String configScript, String resetScript){
        String result = "OK";
        String topic = "lifecycle.configurationResult." + executionId;
        InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.CONFIGURED, result, "configId",false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageConfigurationError("Error while translating internal scheduling message in Json format", executionId);
        }
    }

    @Override
    public void abortConfiguration(String executionId, String tcDescriptorId, String configId){
        //No response message
    }

    @Override
    public void configureInfrastructureMetricCollection(String executionId, String tcDescriptorId, List<MetricInfo> metrics, String nsInstanceId){
        String result = "OK";
        String topic = "lifecycle.configurationResult." + executionId;
        InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.METRIC_CONFIGURED, result, "metricConfigId",false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageConfigurationError("Error while translating internal scheduling message in Json format", executionId);
        }
    }

    @Override
    public void resetConfiguration(String executionId, String tcDescriptorId, String configId){
        String result = "OK";
        String topic = "lifecycle.configurationResult." + executionId;
        InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.CONF_RESET, result, null,false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageConfigurationError("Error while translating internal scheduling message in Json format", executionId);
        }
    }

    @Override
    public void removeInfrastructureMetricCollection(String executionId, String tcDescriptorId, String metricConfigId){
        String result = "OK";
        String topic = "lifecycle.configurationResult." + executionId;
        InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.METRIC_RESET, result, null,false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageConfigurationError("Error while translating internal scheduling message in Json format", executionId);
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
}
