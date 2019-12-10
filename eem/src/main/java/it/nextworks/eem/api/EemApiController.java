package it.nextworks.eem.api;

import it.nextworks.eem.engine.EemService;
import it.nextworks.eem.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import it.nextworks.eem.model.enumerates.ExperimentState;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-03T08:24:25.833Z[GMT]")
@Controller
public class EemApiController implements EemApi {

    private static final Logger log = LoggerFactory.getLogger(EemApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @Autowired
    EemService eemService;

    @org.springframework.beans.factory.annotation.Autowired
    public EemApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    public ResponseEntity<?> eemExperimentExecutionsGet(@ApiParam(value = "Execution state of the experiment") @RequestParam(value = "state", required = false) ExperimentState state) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<List<ExperimentExecution>>(objectMapper.readValue("[ {\n  \"executionId\" : \"executionId\",\n  \"testCaseResult\" : {\n    \"key\" : {\n      \"result\" : \"result\"\n    }\n  },\n  \"state\" : \"INIT\",\n  \"reportUrl\" : \"reportUrl\",\n  \"eemSubscriptionId\" : \"eemSubscriptionId\"\n}, {\n  \"executionId\" : \"executionId\",\n  \"testCaseResult\" : {\n    \"key\" : {\n      \"result\" : \"result\"\n    }\n  },\n  \"state\" : \"INIT\",\n  \"reportUrl\" : \"reportUrl\",\n  \"eemSubscriptionId\" : \"eemSubscriptionId\"\n} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<List<ExperimentExecution>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<List<ExperimentExecution>>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<?> eemExperimentExecutionsIdAbortPost(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        try{
            eemService.abortExperimentExecution(id);
            return new ResponseEntity<Void>(HttpStatus.OK);
        }catch(FailedOperationException e){
            log.debug(null, e);
            log.error(e.getMessage());
            return new ResponseEntity<ErrorInfo>(
                    new ErrorInfo().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).detail(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(NotExistingEntityException e){
            log.debug(null, e);
            log.error(e.getMessage());
            return new ResponseEntity<ErrorInfo>(
                    new ErrorInfo().status(HttpStatus.NOT_FOUND.value()).detail(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> eemExperimentExecutionsIdDelete(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        try{
            eemService.removeExperimentExecutionRecord(id);
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }catch(FailedOperationException e){
            log.debug(null, e);
            log.error(e.getMessage());
            return new ResponseEntity<ErrorInfo>(
                    new ErrorInfo().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).detail(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(NotExistingEntityException e){
            log.debug(null, e);
            log.error(e.getMessage());
            return new ResponseEntity<ErrorInfo>(
                    new ErrorInfo().status(HttpStatus.NOT_FOUND.value()).detail(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> eemExperimentExecutionsIdGet(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                ExperimentExecution response = eemService.getExperimentExecution(id);
                return new ResponseEntity<ExperimentExecution>(response, HttpStatus.OK);
            } catch(FailedOperationException e){
                log.debug(null, e);
                log.error(e.getMessage());
                return new ResponseEntity<ErrorInfo>(
                        new ErrorInfo().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).detail(e.getMessage()),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }catch(NotExistingEntityException e){
                log.debug(null, e);
                log.error(e.getMessage());
                return new ResponseEntity<ErrorInfo>(
                        new ErrorInfo().status(HttpStatus.NOT_FOUND.value()).detail(e.getMessage()),
                        HttpStatus.NOT_FOUND);
            }
        }else{
            log.error("Accept header null or different from application/json");
            return new ResponseEntity<ErrorInfo>(
                    new ErrorInfo().status(HttpStatus.PRECONDITION_FAILED.value()).detail("Accept header null or different from application/json"),
                    HttpStatus.PRECONDITION_FAILED);
        }
    }

    public ResponseEntity<?> eemExperimentExecutionsIdOptions(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<?> eemExperimentExecutionsIdPausePost(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<?> eemExperimentExecutionsIdResumePost(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<?> eemExperimentExecutionsIdRunPost(@ApiParam(value = "" ,required=true )  @RequestBody ExperimentExecutionRequest body, @ApiParam(value = "",required=true) @PathVariable("id") String id, @ApiParam(value = "Determine the type of run. If not present, the default value is RUN_ALL" , allowableValues="RUN_IN_STEPS, RUN_ALL") @RequestParam(value="runType", required=false) String runType) {
        String accept = request.getHeader("Accept");
        //TODO add request validation, add isValid in the data model
        try{
            eemService.runExperimentExecution(body, runType);
            return new ResponseEntity<Void>(HttpStatus.OK);
        }catch(FailedOperationException e){
            log.debug(null, e);
            log.error(e.getMessage());
            return new ResponseEntity<ErrorInfo>(
                    new ErrorInfo().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).detail(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(NotExistingEntityException e){
            log.debug(null, e);
            log.error(e.getMessage());
            return new ResponseEntity<ErrorInfo>(
                    new ErrorInfo().status(HttpStatus.NOT_FOUND.value()).detail(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        }catch(MalformattedElementException e){
            log.debug(null, e);
            log.error(e.getMessage());
            return new ResponseEntity<ErrorInfo>(
                    new ErrorInfo().status(HttpStatus.BAD_REQUEST.value()).detail(e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> eemExperimentExecutionsIdStepPost(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<?> eemExperimentExecutionsOptions() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<?> eemExperimentExecutionsPost() {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                String response = eemService.createExperimentExecutionInstance();
                return new ResponseEntity<String>(response, HttpStatus.CREATED);
            } catch(FailedOperationException e){
                log.debug(null, e);
                log.error(e.getMessage());
                return new ResponseEntity<ErrorInfo>(
                        new ErrorInfo().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).detail(e.getMessage()),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }else{
            log.error("Accept header null or different from application/json");
            return new ResponseEntity<ErrorInfo>(
                    new ErrorInfo().status(HttpStatus.PRECONDITION_FAILED.value()).detail("Accept header null or different from application/json"),
                    HttpStatus.PRECONDITION_FAILED);
        }
    }

    public ResponseEntity<?> eemExperimentNotificationsGet() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<?> eemExperimentNotificationsOptions() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<?> eemExperimentNotificationsPost(@ApiParam(value = "" ,required=true )  @RequestBody ConfigurationChangeNotification body) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<?> eemExperimentSubscriptionsGet() {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<List<ExperimentExecutionSubscription>>(objectMapper.readValue("[ {\n  \"id\" : \"beb7074c-845e-4170-be2e-7e33e31e921a\",\n  \"callbackURI\" : \"http://127.0.0.1/subscribe\",\n  \"subscriptionType\" : \"EXPERIMENT_EXECUTION_CHANGE_STATUS\",\n  \"executionId\" : \"experiment_execution_id\"\n}, {\n  \"id\" : \"beb7074c-845e-4170-be2e-7e33e31e921a\",\n  \"callbackURI\" : \"http://127.0.0.1/subscribe\",\n  \"subscriptionType\" : \"EXPERIMENT_EXECUTION_CHANGE_STATUS\",\n  \"executionId\" : \"experiment_execution_id\"\n} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<List<ExperimentExecutionSubscription>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<List<ExperimentExecutionSubscription>>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<?> eemExperimentSubscriptionsOptions() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<?> eemExperimentSubscriptionsPost(@ApiParam(value = "" ,required=true )  @RequestBody ExperimentExecutionSubscriptionRequest body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                String response = eemService.subscribe(body);
                return new ResponseEntity<String>(response, HttpStatus.CREATED);
            } catch(FailedOperationException e){
                log.debug(null, e);
                log.error(e.getMessage());
                return new ResponseEntity<ErrorInfo>(
                        new ErrorInfo().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).detail(e.getMessage()),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }catch(NotExistingEntityException e){
                log.debug(null, e);
                log.error(e.getMessage());
                return new ResponseEntity<ErrorInfo>(
                        new ErrorInfo().status(HttpStatus.NOT_FOUND.value()).detail(e.getMessage()),
                        HttpStatus.NOT_FOUND);
            }
        }else{
            log.error("Accept header null or different from application/json");
            return new ResponseEntity<ErrorInfo>(
                    new ErrorInfo().status(HttpStatus.PRECONDITION_FAILED.value()).detail("Accept header null or different from application/json"),
                    HttpStatus.PRECONDITION_FAILED);
        }
    }

    public ResponseEntity<?> eemExperimentSubscriptionsSubscriptionIdDelete(@ApiParam(value = "",required=true) @PathVariable("subscriptionId") String subscriptionId) {
        String accept = request.getHeader("Accept");
        try{
            eemService.unsubscribe(subscriptionId);
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }catch(FailedOperationException e){
            log.debug(null, e);
            log.error(e.getMessage());
            return new ResponseEntity<ErrorInfo>(
                    new ErrorInfo().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).detail(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(NotExistingEntityException e){
            log.debug(null, e);
            log.error(e.getMessage());
            return new ResponseEntity<ErrorInfo>(
                    new ErrorInfo().status(HttpStatus.NOT_FOUND.value()).detail(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> eemExperimentSubscriptionsSubscriptionIdGet(@ApiParam(value = "",required=true) @PathVariable("subscriptionId") String subscriptionId) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<ExperimentExecutionSubscription>(objectMapper.readValue("{\n  \"id\" : \"beb7074c-845e-4170-be2e-7e33e31e921a\",\n  \"callbackURI\" : \"http://127.0.0.1/subscribe\",\n  \"subscriptionType\" : \"EXPERIMENT_EXECUTION_CHANGE_STATUS\",\n  \"executionId\" : \"experiment_execution_id\"\n}", ExperimentExecutionSubscription.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<ExperimentExecutionSubscription>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<ExperimentExecutionSubscription>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<?> eemExperimentSubscriptionsSubscriptionIdOptions(@ApiParam(value = "",required=true) @PathVariable("subscriptionId") String subscriptionId) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<?> eemOptions() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<?> listVersionsv1() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }
}
