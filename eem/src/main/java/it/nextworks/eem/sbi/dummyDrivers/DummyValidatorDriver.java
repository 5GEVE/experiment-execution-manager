package it.nextworks.eem.sbi.dummyDrivers;

import it.nextworks.eem.sbi.interfaces.ValidatorServiceProviderInterface;
import it.nextworks.eem.sbi.jenkins.JenkinsDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyValidatorDriver implements ValidatorServiceProviderInterface {

    private static final Logger log = LoggerFactory.getLogger(DummyValidatorDriver.class);

    public DummyValidatorDriver() {
        log.debug("Initializing Dummy Driver");
    }

    @Override
    public void configureExperiment(String executionId){

    }

    @Override
    public void startTcValidation(String executionId, String tcDescriptorId){

    }

    @Override
    public void stopTcValidation(String executionId, String tcDescriptorId){

    }

    @Override
    public void queryValidationResult(String executionId, String tcDescriptorId){

    }

    @Override
    public void terminateExperiment(String executionId){

    }
}
