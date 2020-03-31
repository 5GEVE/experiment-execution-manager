package it.nextworks.eem.sbi.dummyDrivers;

import it.nextworks.eem.sbi.interfaces.ExecutorServiceProviderInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyExecutorDriver implements ExecutorServiceProviderInterface {

    private static final Logger log = LoggerFactory.getLogger(DummyExecutorDriver.class);

    public DummyExecutorDriver() {
        log.debug("Initializing Dummy Driver");
    }

    @Override
    public void runTestCase(String executionId, String tcDescriptorId, String testCaseFile){

    }

    @Override
    public void abortTestCase(String executionId, String tcDescriptorId){

    }
}
