package it.nextworks.eem.sbi;

import it.nextworks.eem.configuration.ConfigurationParameters;
import it.nextworks.eem.sbi.dummyDrivers.DummyExecutorDriver;
import it.nextworks.eem.sbi.enums.ExecutorType;
import it.nextworks.eem.sbi.interfaces.ExecutorServiceProviderInterface;
import it.nextworks.eem.sbi.jenkins.JenkinsDriver;
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

@Service
public class ExecutorService implements ExecutorServiceProviderInterface {

    private static final Logger log = LoggerFactory.getLogger(ExecutorService.class);

    private ExecutorServiceProviderInterface driver;

    @Value("${executor.type}")
    private ExecutorType executorType;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier(ConfigurationParameters.eemQueueExchange)
    private TopicExchange messageExchange;

    @Value("${eem.jenkins.uri}")
    private String jenkinsURI;

    @Value("${eem.jenkins.username}")
    private String jenkinsUsername;

    @Value("${eem.jenkins.password}")
    private String jenkinsPassword;

    @Value("${eem.jenkins.validation.url}")
    private String jenkinsValidationBaseUrl;

    @Value("${runtime.configurator.uri}")
    private String runTimeConfiguratorURI;

    @PostConstruct
    public void init() throws URISyntaxException {
        log.debug("Initializing Executor driver");
        if (executorType.equals(ExecutorType.RC))
            this.driver = RCDriver.getInstance(jenkinsURI, rabbitTemplate, messageExchange);
        else if (executorType.equals(ExecutorType.JENKINS))
            this.driver = JenkinsDriver.getInstance(jenkinsURI, jenkinsUsername, jenkinsPassword, jenkinsValidationBaseUrl, rabbitTemplate, messageExchange);
        else if (executorType.equals(ExecutorType.DUMMY))
            this.driver = new DummyExecutorDriver(rabbitTemplate, messageExchange);
        else
            log.error("Wrong configuration for Executor service.");
    }

    @Override
    public void runTestCase(String executionId, String tcDescriptorId, String executionScript){
        driver.runTestCase(executionId, tcDescriptorId, executionScript);
    }

    @Override
    public void abortTestCase(String executionId, String tcDescriptorId){
        driver.abortTestCase(executionId, tcDescriptorId);
    }
}
