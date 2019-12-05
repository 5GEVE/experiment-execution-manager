package it.nextworks.eem.engine;

import it.nextworks.eem.model.ExperimentExecutionStateChangeNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EemSubscriptionService{

    private static final Logger log = LoggerFactory.getLogger(EemSubscriptionService.class);

    //Map<experimentExecutionId, List<subscriptionId>>
    private Map<String, List<String>> subscriptions = new HashMap<>();

    public void notifyExperimentExecutionStatusChange(ExperimentExecutionStateChangeNotification msg){
        //TODO usare msg.getExperimentExecutionId() come chiave, prendere tutti i subscriptionId e fare query in db. A questo punto notificare tutti gli URI
    }
}
