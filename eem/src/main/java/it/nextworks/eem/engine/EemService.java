package it.nextworks.eem.engine;

import it.nextworks.eem.api.EemApiController;
import it.nextworks.eem.model.ExperimentExecutionRequest;
import it.nextworks.eem.model.ExperimentExecution;
import it.nextworks.eem.model.ExperimentExecutionSubscriptionRequest;
import it.nextworks.eem.model.ExperimentState;
import it.nextworks.eem.repos.ExperimentExecutionRepository;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EemService{

    private static final Logger log = LoggerFactory.getLogger(EemService.class);

    @Autowired
    private EemSubscriptionService subscriptionService;

    @Autowired
    private ExperimentExecutionRepository experimentExecutionRepository;

    public String createExperimentExecutionInstance() throws FailedOperationException{
        String executionId = UUID.randomUUID().toString();
        log.info("Creating new Experiment Execution with Id {}", executionId);
        ExperimentExecution experimentExecution = new ExperimentExecution();
        experimentExecution.setExecutionId(executionId);
        experimentExecution.setState(ExperimentState.INIT);
        experimentExecutionRepository.saveAndFlush(experimentExecution);
        log.debug("Experiment Execution created and stored");
        return executionId;
    }

    public void runExperimentExecution(ExperimentExecutionRequest request) throws FailedOperationException, NotExistingEntityException, MalformattedElementException {

    }

    public ExperimentExecution getExperimentExecution(String experimentExecutionId) throws FailedOperationException, NotExistingEntityException{
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
