package it.nextworks.eem.sbi.interfaces;

import it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.common.messages.GeneralizedQueryRequest;
import it.nextworks.openapi.msno.model.NsInstance;

public interface MultiSiteOrchestratorProviderInterface {

    NsInstance queryNs(GeneralizedQueryRequest request) throws FailedOperationException, MalformattedElementException;
}
