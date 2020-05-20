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

    @Value("${eem.jenkins.uri}")
    private String jenkinsURI;

    @Value("${eem.jenkins.username}")
    private String jenkinsUsername;

    @Value("${eem.jenkins.password}")
    private String jenkinsPassword;

    @PostConstruct
    public void init() throws URISyntaxException {
        log.debug("Initializing Configurator driver");
        if (configuratorType.equals(ConfiguratorType.RC))
            this.driver = RCDriver.getInstance(jenkinsURI, jenkinsUsername, jenkinsPassword, rabbitTemplate, messageExchange);
        else if (configuratorType.equals((ConfiguratorType.DUMMY)))
            this.driver = new DummyConfiguratorDriver(rabbitTemplate, messageExchange);
        else
            log.error("Wrong configuration for Configurator service.");
    }

    @Override
    public void applyConfiguration(String executionId, String tcDescriptorId, String configScript){
        driver.applyConfiguration(executionId, tcDescriptorId, configScript);
    }

    @Override
    public void abortConfiguration(String executionId, String tcDescriptorId){
        driver.abortConfiguration(executionId, tcDescriptorId);
    }

    @Override
    public void configureInfrastructureMetricCollection(String executionId, String tcDescriptorId, List<MetricInfo> metrics){
        driver.configureInfrastructureMetricCollection(executionId, tcDescriptorId, metrics);
    }

    @Override
    public void resetConfiguration(String executionId, String tcDescriptorId, String resetScript){
        driver.resetConfiguration(executionId, tcDescriptorId, resetScript);
    }

    @Override
    public void removeInfrastructureMetricCollection(String executionId, String tcDescriptorId, List<String> metricConfigIds){
        driver.removeInfrastructureMetricCollection(executionId, tcDescriptorId, metricConfigIds);
    }
}
