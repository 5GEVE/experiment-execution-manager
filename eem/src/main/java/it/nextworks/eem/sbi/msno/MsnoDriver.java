package it.nextworks.eem.sbi.msno;

import it.nextworks.eem.sbi.interfaces.MultiSiteOrchestratorProviderInterface;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.common.messages.GeneralizedQueryRequest;
import it.nextworks.openapi.ApiClient;
import it.nextworks.openapi.msno.DefaultApi;
import it.nextworks.openapi.msno.model.NsInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MsnoDriver implements MultiSiteOrchestratorProviderInterface {

    // static variable single_instance of type JenkinsDriver
    private static MsnoDriver single_instance = null;

    private static final Logger log = LoggerFactory.getLogger(MsnoDriver.class);

    private DefaultApi restClient;

    private String version = "v1";
    private String accept = "application/json";
    private String contentType = "application/json";
    private String authorization = null;		//TODO: this is to be fixed - it should be the token

    // private constructor restricted to this class itself
    private MsnoDriver(String msnoHost){
        log.debug("Initializing Msno Driver: uri {}", msnoHost);
        ApiClient ac = new ApiClient();
        String url = "http://" + msnoHost + "/nslcm/v1";
        restClient = new DefaultApi(ac.setBasePath(url));
    }

    // static method to create instance of MsnoDriver class
    public static MsnoDriver getInstance(String msnoHost){
        if (single_instance == null)
            single_instance = new MsnoDriver(msnoHost);
        else
            log.debug("Msno Driver already instantiated: uri {}", msnoHost);
        return single_instance;
    }

    @Override
    public NsInstance queryNs(GeneralizedQueryRequest request) throws FailedOperationException, MalformattedElementException {
        if (request == null)
            throw new MalformattedElementException("Query NS request is null");
        request.isValid();
        try {
            String nsInstanceId = request.getFilter().getParameters().get("NS_ID");
            log.debug("Building query NS request in SOL 005 format");
            NsInstance nsInstance = restClient.nsInstancesNsInstanceIdGet(nsInstanceId, version, accept, contentType, authorization);
            if (nsInstance == null)
                throw new NotExistingEntityException("NS instance not found");
            log.debug("NsInstance correctly retrieved");
            return nsInstance;
        } catch (Exception e) {
            throw new FailedOperationException("Failure when interacting with NFVO : " + e.getMessage());
        }
        /*
        return null;
        */
    }
}
