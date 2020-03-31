package it.nextworks.eem.sbi.dummyDrivers;

import it.nextworks.eem.sbi.interfaces.ConfiguratorServiceProviderInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyConfiguratorDriver implements ConfiguratorServiceProviderInterface {

    private static final Logger log = LoggerFactory.getLogger(DummyConfiguratorDriver.class);

    public DummyConfiguratorDriver() {
        log.debug("Initializing Dummy Driver");
    }

    @Override
    public void configureExperiment(String executionId){

    }
}
