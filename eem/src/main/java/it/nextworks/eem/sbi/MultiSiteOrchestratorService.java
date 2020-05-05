package it.nextworks.eem.sbi;

import it.nextworks.eem.sbi.dummyDrivers.DummyMultiSiteOrchestratorDriver;
import it.nextworks.eem.sbi.enums.MultiSiteOrchestratorType;
import it.nextworks.eem.sbi.interfaces.MultiSiteOrchestratorProviderInterface;
import it.nextworks.eem.sbi.msno.MsnoDriver;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.common.messages.GeneralizedQueryRequest;
import it.nextworks.openapi.msno.model.NsInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URISyntaxException;

@Service
public class MultiSiteOrchestratorService implements MultiSiteOrchestratorProviderInterface {

    private static final Logger log = LoggerFactory.getLogger(MultiSiteOrchestratorService.class);

    private MultiSiteOrchestratorProviderInterface driver;

    @Value("${multi.site.orchestrator.type}")
    private MultiSiteOrchestratorType orchestratorType;

    @Value("${msno.host}")
    private String msnoHost;

    @PostConstruct
    public void init(){
        log.debug("Initializing Multi-site Orchestrator driver");
        if (orchestratorType.equals(MultiSiteOrchestratorType.MSNO))
            this.driver = MsnoDriver.getInstance(msnoHost);
        else if (orchestratorType.equals(MultiSiteOrchestratorType.DUMMY))
            this.driver = new DummyMultiSiteOrchestratorDriver();
        else
            log.error("Wrong configuration for Multi-site Orchestrator service.");
    }

    @Override
    public NsInstance queryNs(GeneralizedQueryRequest request) throws FailedOperationException, MalformattedElementException{
        return driver.queryNs(request);
    }
}
