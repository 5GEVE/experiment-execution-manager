package it.nextworks.eem.engine;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.nextworks.eem.engine.messages.*;
import it.nextworks.eem.model.enumerates.ExperimentState;
import it.nextworks.nfvmano.catalogue.blueprint.elements.CtxDescriptor;
import it.nextworks.nfvmano.catalogue.blueprint.elements.TestCaseDescriptor;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//TODO persist??
public class ExperimentExecutionInstanceManager {

    private static final Logger log = LoggerFactory.getLogger(ExperimentExecutionInstanceManager.class);

    private String executionId;
    private ExperimentState state;

    private VsDescriptor vsDescriptor;
    private List<CtxDescriptor> ctxDescriptors = new ArrayList<>();
    private List<TestCaseDescriptor> tcDescriptors = new ArrayList<>();

    public ExperimentExecutionInstanceManager(String executionId){
        this.executionId = executionId;
        this.state = ExperimentState.INIT;
    }

    /**
     * Method used to receive messages about experiment execution LCM from the Rabbit MQ
     *
     * @param message received message
     */
    public void receiveMessage(String message) {
        log.info("Received message for Experiment Execution {} \n {}", executionId, message);
        try {
            ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
                    .modules(new JavaTimeModule())
                    .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                    .build();
            InternalMessage im = mapper.readValue(message, InternalMessage.class);
            InternalMessageType imt = im.getType();

            switch (imt) {
                case RUN_ALL_EXPERIMENT: {
                    log.debug("Processing request to run all the experiment execution with Id {}", executionId);
                    RunAllExperimentInternalMessage msg = (RunAllExperimentInternalMessage) im;
                    processRunAllRequest(msg);
                    break;
                }
                case RUN_STEP_EXPERIMENT: {
                    log.debug("Processing request to run step by step the experiment execution with Id {}", executionId);
                    RunStepExperimentInternalMessage msg = (RunStepExperimentInternalMessage) im;
                    processRunStepRequest(msg);
                    break;
                }
                case PAUSE_EXPERIMENT: {
                    log.debug("Processing request to pause the experiment execution with Id {}", executionId);
                    PauseExperimentInternalMessage msg = (PauseExperimentInternalMessage) im;
                    processPauseRequest(msg);
                    break;
                }
                case RESUME_EXPERIMENT: {
                    log.debug("Processing request to resume the experiment execution with Id {}", executionId);
                    ResumeExperimentInternalMessage msg = (ResumeExperimentInternalMessage) im;
                    processResumeRequest(msg);
                    break;
                }
                case STEP_EXPERIMENT: {
                    log.debug("Processing request to run a step of the experiment execution with Id {}", executionId);
                    StepExperimentInternalMessage msg = (StepExperimentInternalMessage) im;
                    processStepRequest(msg);
                    break;
                }
                case ABORT_EXPERIMENT: {
                    log.debug("Processing request to abort the experiment execution with Id {}", executionId);
                    AbortExperimentInternalMessage msg = (AbortExperimentInternalMessage) im;
                    processAbortRequest(msg);
                    break;
                }
                default:
                    log.error("Received message with not supported type. Skipping.");
                    break;
            }
        //TODO handle other errors
        } catch (JsonParseException e) {
            manageExperimentExecutionError("Error while parsing message: " + e.getMessage());
        } catch (JsonMappingException e) {
            manageExperimentExecutionError("Error in Json mapping: " + e.getMessage());
        } catch (IOException e) {
            manageExperimentExecutionError("IO error when receiving json message: " + e.getMessage());
        }
    }

    private void processRunAllRequest(RunAllExperimentInternalMessage msg){

    }

    private void processRunStepRequest(RunStepExperimentInternalMessage msg){

    }

    private void processPauseRequest(PauseExperimentInternalMessage msg){

    }

    private void processResumeRequest(ResumeExperimentInternalMessage msg){

    }

    private void processStepRequest(StepExperimentInternalMessage msg){

    }

    private void processAbortRequest(AbortExperimentInternalMessage msg){

    }

    private void manageExperimentExecutionError(String errorMessage){
        log.error("Exeperiment Execution failed : {}", errorMessage);
        //TODO handle error
    }
}
