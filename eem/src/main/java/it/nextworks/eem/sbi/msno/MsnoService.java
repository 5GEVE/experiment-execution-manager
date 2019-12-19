package it.nextworks.eem.sbi.msno;

import it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MethodNotImplementedException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.common.messages.GeneralizedQueryRequest;
import it.nextworks.openapi.ApiClient;
import it.nextworks.openapi.msno.DefaultApi;
import it.nextworks.openapi.msno.model.NsInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MsnoService {

    private static final Logger log = LoggerFactory.getLogger(MsnoService.class);

    private DefaultApi restClient;

    @Value("${msno.host}")
    private String msnoHost;

    private String version = "v1";
    private String accept = "application/json";
    private String contentType = "application/json";
    private String authorization = null;		//TODO: this is to be fixed - it should be the token

    public MsnoService() {}

    @PostConstruct
    private void initMsnoClient() {
        log.debug("Initializing MSNO REST client");
        /*
        ApiClient ac = new ApiClient();
        String url = "http://" + msnoHost + "/nslcm/v1";
        ac.setBasePath(url);
         */
    }

    public NsInstance queryNs(GeneralizedQueryRequest request) throws FailedOperationException, MalformattedElementException {
        /*
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
        */
        return null;
    }
}
