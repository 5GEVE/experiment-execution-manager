package it.nextworks.eem.sbi;

import it.nextworks.eem.configuration.ConfigurationParameters;
import it.nextworks.eem.repo.ExperimentExecutionRepository;
import it.nextworks.eem.sbi.dummyDrivers.DummyValidatorDriver;
import it.nextworks.eem.sbi.enums.ValidatorType;
import it.nextworks.eem.sbi.expcatalogue.ExperimentCatalogueService;
import it.nextworks.eem.sbi.interfaces.ValidatorServiceProviderInterface;
import it.nextworks.eem.sbi.jenkins.JenkinsDriver;
import it.nextworks.eem.sbi.rav.RAVDriver;
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
public class ValidatorService implements ValidatorServiceProviderInterface {

    private static final Logger log = LoggerFactory.getLogger(ValidatorService.class);

    private ValidatorServiceProviderInterface driver;

    @Value("${validator.type}")
    private ValidatorType validatorType;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier(ConfigurationParameters.eemQueueExchange)
    private TopicExchange messageExchange;

    @Autowired
    private ExperimentExecutionRepository experimentExecutionRepository;

    @Autowired
    private ExperimentCatalogueService catalogueService;

    @Value("${eem.jenkins.uri}")
    private String jenkinsURI;

    @Value("${eem.jenkins.username}")
    private String jenkinsUsername;

    @Value("${eem.jenkins.password}")
    private String jenkinsPassword;

    @Value("${monitoring.address}")
    private String monitoringAddress;

    @Value("${eem.jenkins.validation.url}")
    private String jenkinsValidationBaseUrl;

    @Value("${monitoring.port}")
    private String monitoringPort;

    @Value("${rav.uri}")
    private String ravURI;

    @PostConstruct
    public void init() throws URISyntaxException {
        log.debug("Initializing Validator driver");
        if (validatorType.equals(ValidatorType.RAV))
            this.driver = RAVDriver.getInstance(ravURI, monitoringAddress, monitoringPort, catalogueService, experimentExecutionRepository, rabbitTemplate, messageExchange);
        else if (validatorType.equals(ValidatorType.JENKINS))
            this.driver = JenkinsDriver.getInstance(jenkinsURI, jenkinsUsername, jenkinsPassword, jenkinsValidationBaseUrl, rabbitTemplate, messageExchange);
        else if (validatorType.equals(ValidatorType.DUMMY))
            this.driver = new DummyValidatorDriver(rabbitTemplate, messageExchange);
        else
            log.error("Wrong configuration for Executor service.");
    }

    @Override
    public void configureExperiment(String executionId){
        driver.configureExperiment(executionId);
    }

    @Override
    public void startTcValidation(String executionId, String tcDescriptorId){
        driver.startTcValidation(executionId, tcDescriptorId);
    }

    @Override
    public void stopTcValidation(String executionId, String tcDescriptorId){
        driver.stopTcValidation(executionId, tcDescriptorId);
    }

    @Override
    public void queryValidationResult(String executionId, String tcDescriptorId){
        driver.queryValidationResult(executionId, tcDescriptorId);
    }

    @Override
    public void terminateExperiment(String executionId){
        driver.terminateExperiment(executionId);
    }
}
