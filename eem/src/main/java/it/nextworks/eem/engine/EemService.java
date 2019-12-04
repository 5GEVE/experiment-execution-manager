package it.nextworks.eem.engine;

import it.nextworks.eem.model.ExperimentExecutionRequest;
import it.nextworks.eem.model.ExperimentExecutionResponse;
import it.nextworks.eem.model.ExperimentExecutionSubscriptionRequest;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EemService{

    @Autowired
    EemSubscriptionService subscriptionService;

    public String createExperimentExecutionInstance() throws FailedOperationException{
        return null;
    }

    public void runExperimentExecution(ExperimentExecutionRequest request) throws FailedOperationException, NotExistingEntityException, MalformattedElementException {

    }

    public ExperimentExecutionResponse getExperimentExecution(String experimentExecutionId) throws FailedOperationException, NotExistingEntityException{
        return null;
    }

    public void abortExperimentExecution(String experimentExecutionId) throws FailedOperationException, NotExistingEntityException{

    }

    public void removeExperimentExecutionRecord(String experimentExecutionId) throws FailedOperationException, NotExistingEntityException{

    }

    public String subscribe(ExperimentExecutionSubscriptionRequest subscriptionRequest) throws FailedOperationException, NotExistingEntityException{
        return null;
        //TODO salvare in DB la subscription e aggiungere entry nella map del subscriptionService
    }

    public void unsubscribe(String subscriptionId) throws FailedOperationException, NotExistingEntityException{
        //TODO rimuovere dal DB la subscription e rimuovere entry nella map del subscriptionService
    }
}
