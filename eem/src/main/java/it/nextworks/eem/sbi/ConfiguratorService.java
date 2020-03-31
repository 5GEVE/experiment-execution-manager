package it.nextworks.eem.sbi;

import it.nextworks.eem.configuration.ConfigurationParameters;
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

    @PostConstruct
    public void init() {
        log.debug("Initializing Configurator driver");
        if (configuratorType.equals(ConfiguratorType.RC))
            this.driver = RCDriver.getInstance(rabbitTemplate, messageExchange);
        else
            log.error("Wrong configuration for Configurator service.");
    }

    @Override
    public void configureExperiment(String executionId){
        driver.configureExperiment(executionId);
    }
}
