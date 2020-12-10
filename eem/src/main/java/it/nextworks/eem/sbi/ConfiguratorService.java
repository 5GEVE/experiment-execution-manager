package it.nextworks.eem.sbi;

import it.nextworks.eem.configuration.ConfigurationParameters;
import it.nextworks.eem.model.MetricInfo;
import it.nextworks.eem.sbi.dummyDrivers.DummyConfiguratorDriver;
import it.nextworks.eem.sbi.enums.ConfiguratorType;
import it.nextworks.eem.sbi.interfaces.ConfiguratorServiceProviderInterface;
import it.nextworks.eem.sbi.runtimeConfigurator.RCDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URISyntaxException;
import java.util.List;

@Service
public class ConfiguratorService implements ConfiguratorServiceProviderInterface {

    private static final Logger log = LoggerFactory.getLogger(ConfiguratorService.class);

    private ConfiguratorServiceProviderInterface driver;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier(ConfigurationParameters.eemQueueExchange)
    private TopicExchange messageExchange;

    @Value("${configurator.type}")
    private ConfiguratorType configuratorType;

    @Value("${runtime.configurator.uri}")
    private String runTimeConfiguratorURI;

    @PostConstruct
    public void init() throws URISyntaxException {
        log.debug("Initializing Configurator driver");
        if (configuratorType.equals(ConfiguratorType.RC))
            this.driver = RCDriver.getInstance(runTimeConfiguratorURI, rabbitTemplate, messageExchange);
        else if (configuratorType.equals((ConfiguratorType.DUMMY)))
            this.driver = new DummyConfiguratorDriver(rabbitTemplate, messageExchange);
        else
            log.error("Wrong configuration for Configurator service.");
    }

    @Override
    public void applyConfiguration(String executionId, String tcDescriptorId, String configScript, String resetScript){
        driver.applyConfiguration(executionId, tcDescriptorId, configScript, resetScript);
    }

    @Override
    public void abortConfiguration(String executionId, String tcDescriptorId, String configId){
        driver.abortConfiguration(executionId, tcDescriptorId, configId);
    }

    @Override
    public void configureInfrastructureMetricCollection(String executionId, String tcDescriptorId, List<MetricInfo> metrics, String nsInstanceId){
        driver.configureInfrastructureMetricCollection(executionId, tcDescriptorId, metrics, nsInstanceId);
    }

    @Override
    public void resetConfiguration(String executionId, String tcDescriptorId, String configId){
        driver.resetConfiguration(executionId, tcDescriptorId, configId);
    }

    @Override
    public void removeInfrastructureMetricCollection(String executionId, String tcDescriptorId, String metricConfigId){
        driver.removeInfrastructureMetricCollection(executionId, tcDescriptorId, metricConfigId);
    }
}
