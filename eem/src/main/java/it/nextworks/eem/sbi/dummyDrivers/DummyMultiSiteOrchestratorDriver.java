package it.nextworks.eem.sbi.dummyDrivers;

import it.nextworks.eem.sbi.interfaces.MultiSiteOrchestratorProviderInterface;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.common.messages.GeneralizedQueryRequest;
import it.nextworks.openapi.msno.model.NsInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyMultiSiteOrchestratorDriver implements MultiSiteOrchestratorProviderInterface {

    private static final Logger log = LoggerFactory.getLogger(DummyMultiSiteOrchestratorDriver.class);

    public DummyMultiSiteOrchestratorDriver() {
        log.debug("Initializing Dummy Multi Site Orchestrator Driver");
    }

    @Override
    public NsInstance queryNs(GeneralizedQueryRequest request) throws FailedOperationException, MalformattedElementException{
        return new NsInstance();
    }
}
