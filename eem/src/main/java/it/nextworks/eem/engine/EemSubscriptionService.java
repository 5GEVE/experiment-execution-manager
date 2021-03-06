package it.nextworks.eem.engine;

import it.nextworks.eem.model.ExperimentExecutionStateChangeNotification;
import it.nextworks.eem.model.ExperimentExecutionSubscription;
import it.nextworks.eem.model.ExperimentExecutionSubscriptionRequest;
import it.nextworks.eem.model.enumerate.ExperimentState;
import it.nextworks.eem.model.enumerate.SubscriptionType;
import it.nextworks.eem.repo.ExperimentExecutionSubscriptionRepository;
import it.nextworks.eem.sbi.lcm.ExperimentLcmService;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EemSubscriptionService{

    private static final Logger log = LoggerFactory.getLogger(EemSubscriptionService.class);

    @Autowired
    private ExperimentExecutionSubscriptionRepository experimentExecutionSubscriptionRepository;

    @Autowired
    private ExperimentLcmService lcmService;

    public List<ExperimentExecutionSubscription> getExperimentExecutionSubscriptions() throws FailedOperationException {
        return experimentExecutionSubscriptionRepository.findAll();
    }

    public ExperimentExecutionSubscription getExperimentExecutionSubscription(String subscriptionId) throws FailedOperationException, NotExistingEntityException {
        Optional<ExperimentExecutionSubscription> experimentExecutionSubscriptionOptional = experimentExecutionSubscriptionRepository.findBySubscriptionId(subscriptionId);
        if(!experimentExecutionSubscriptionOptional.isPresent())
            throw new NotExistingEntityException(String.format("Experiment Execution Subscription with Id %s not found", subscriptionId));
        log.debug("{}", experimentExecutionSubscriptionOptional.get().toString());
        return experimentExecutionSubscriptionOptional.get();
    }

    public String subscribe(ExperimentExecutionSubscriptionRequest subscriptionRequest) throws FailedOperationException{
        String subscriptionId = UUID.randomUUID().toString();//TODO use the id generated from the db?
        log.info("Creating new Experiment Execution Subscription with Id {}", subscriptionId);
        ExperimentExecutionSubscription experimentExecutionSubscription = new ExperimentExecutionSubscription();
        experimentExecutionSubscription.subscriptionId(subscriptionId)
                .subscriptionType(subscriptionRequest.getSubscriptionType())
                .executionId(subscriptionRequest.getExecutionId())
                .callbackURI(subscriptionRequest.getCallbackURI());
        if(subscriptionRequest.getSubscriptionType().equals(SubscriptionType.STATE))
            experimentExecutionSubscriptionRepository.saveAndFlush(experimentExecutionSubscription);
        else
            throw new FailedOperationException("Only state change subscriptions are currently supported");
        log.info("Experiment Execution Subscription with Id {} created and stored", experimentExecutionSubscription.getSubscriptionId());
        log.debug("{}", experimentExecutionSubscription.toString());
        return subscriptionId;
    }

    public void unsubscribe(String subscriptionId) throws FailedOperationException, NotExistingEntityException {
        Optional<ExperimentExecutionSubscription> experimentExecutionSubscription = experimentExecutionSubscriptionRepository.findBySubscriptionId(subscriptionId);
        if(!experimentExecutionSubscription.isPresent())
            throw new NotExistingEntityException(String.format("Experiment Execution Subscription with Id %s not found", subscriptionId));
        experimentExecutionSubscriptionRepository.delete(experimentExecutionSubscription.get());
        log.info("Deleted Experiment Execution Subscription with Id {}", subscriptionId);
    }

    public void deleteAllSubscriptions(String executionId){
        log.info("Deleting all Experiment Execution Subscriptions of Experiment Execution with Id {}", executionId);
        List<ExperimentExecutionSubscription> experimentExecutionSubscriptions = experimentExecutionSubscriptionRepository.findByExecutionId(executionId);
        for(ExperimentExecutionSubscription subscription : experimentExecutionSubscriptions) {
            log.info("Experiment Execution Subscription with Id {} deleted", subscription.getSubscriptionId());
            experimentExecutionSubscriptionRepository.delete(subscription);
        }
    }

    public void notifyExperimentExecutionStateChange(ExperimentExecutionStateChangeNotification msg, ExperimentState previousState){
        log.info("Notifying Experiment Execution with Id {} state change from {} to {}", msg.getExperimentExecutionId(), previousState, msg.getCurrentStatus());
        List<ExperimentExecutionSubscription> subscriptions = experimentExecutionSubscriptionRepository.findByExecutionId(msg.getExperimentExecutionId());
        //Add subscribers to all experiment executions
        subscriptions.addAll(experimentExecutionSubscriptionRepository.findByExecutionId("*"));
        for(ExperimentExecutionSubscription subscription : subscriptions)
            lcmService.notifyExperimentExecutionStateChange(subscription.getCallbackURI(), msg);
    }
}
