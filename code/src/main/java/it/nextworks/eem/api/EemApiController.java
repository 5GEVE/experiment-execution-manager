package it.nextworks.eem.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import it.nextworks.eem.model.ConfigurationChangeNotification;
import it.nextworks.eem.model.ErrorInfo;
import it.nextworks.eem.model.ExperimentExecutionRequest;
import it.nextworks.eem.model.ExperimentExecutionResponse;
import it.nextworks.eem.model.ExperimentExecutionSubscriptionRequest;
import it.nextworks.eem.model.ExperimentExecutionSubscriptionResponse;
import it.nextworks.eem.model.ExperimentState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-29T09:22:19.643Z[GMT]")
@Controller
public class EemApiController implements EemApi {

    private static final Logger log = LoggerFactory.getLogger(EemApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public EemApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    public ResponseEntity<List<ExperimentExecutionResponse>> eemExperimentExecutionsGet(@ApiParam(value = "Execution state of the experiment") @Valid @RequestParam(value = "state", required = false) ExperimentState state) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<List<ExperimentExecutionResponse>>(objectMapper.readValue("[ {\n  \"executionId\" : \"executionId\",\n  \"testCaseResult\" : {\n    \"key\" : {\n      \"result\" : \"result\"\n    }\n  },\n  \"state\" : \"INIT\",\n  \"reportUrl\" : \"reportUrl\",\n  \"eemSubscriptionId\" : \"eemSubscriptionId\"\n}, {\n  \"executionId\" : \"executionId\",\n  \"testCaseResult\" : {\n    \"key\" : {\n      \"result\" : \"result\"\n    }\n  },\n  \"state\" : \"INIT\",\n  \"reportUrl\" : \"reportUrl\",\n  \"eemSubscriptionId\" : \"eemSubscriptionId\"\n} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<List<ExperimentExecutionResponse>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<List<ExperimentExecutionResponse>>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentExecutionsIdAbortPost(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentExecutionsIdDelete(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<ExperimentExecutionResponse> eemExperimentExecutionsIdGet(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<ExperimentExecutionResponse>(objectMapper.readValue("{\n  \"executionId\" : \"executionId\",\n  \"testCaseResult\" : {\n    \"key\" : {\n      \"result\" : \"result\"\n    }\n  },\n  \"state\" : \"INIT\",\n  \"reportUrl\" : \"reportUrl\",\n  \"eemSubscriptionId\" : \"eemSubscriptionId\"\n}", ExperimentExecutionResponse.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<ExperimentExecutionResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<ExperimentExecutionResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentExecutionsIdOptions(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentExecutionsIdPausePost(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentExecutionsIdResumePost(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentExecutionsIdRunPost(@ApiParam(value = "" ,required=true )  @Valid @RequestBody ExperimentExecutionRequest body,@ApiParam(value = "",required=true) @PathVariable("id") String id,@ApiParam(value = "Determine the type of run. If not present, the default value is RUN_ALL" , allowableValues="RUN_IN_STEPS, RUN_ALL") @RequestHeader(value="runType", required=false) String runType) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentExecutionsIdStepPost(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentExecutionsOptions() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<String> eemExperimentExecutionsPost() {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<String>(objectMapper.readValue("\"\"", String.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<String>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentNotificationsGet() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentNotificationsOptions() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentNotificationsPost(@ApiParam(value = "" ,required=true )  @Valid @RequestBody ConfigurationChangeNotification body) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<List<ExperimentExecutionSubscriptionResponse>> eemExperimentSubscriptionsGet() {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<List<ExperimentExecutionSubscriptionResponse>>(objectMapper.readValue("[ {\n  \"id\" : \"beb7074c-845e-4170-be2e-7e33e31e921a\",\n  \"callbackURI\" : \"http://127.0.0.1/subscribe\",\n  \"subscriptionType\" : \"EXPERIMENT_EXECUTION_CHANGE_STATUS\",\n  \"executionId\" : \"experiment_execution_id\"\n}, {\n  \"id\" : \"beb7074c-845e-4170-be2e-7e33e31e921a\",\n  \"callbackURI\" : \"http://127.0.0.1/subscribe\",\n  \"subscriptionType\" : \"EXPERIMENT_EXECUTION_CHANGE_STATUS\",\n  \"executionId\" : \"experiment_execution_id\"\n} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<List<ExperimentExecutionSubscriptionResponse>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<List<ExperimentExecutionSubscriptionResponse>>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentSubscriptionsOptions() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<String> eemExperimentSubscriptionsPost(@ApiParam(value = "" ,required=true )  @Valid @RequestBody ExperimentExecutionSubscriptionRequest body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<String>(objectMapper.readValue("\"45c0d65a-0cc0-4c20-9712-0fd460c6a40c\"", String.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<String>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentSubscriptionsSubscriptionIdDelete(@ApiParam(value = "",required=true) @PathVariable("subscriptionId") String subscriptionId) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<ExperimentExecutionSubscriptionResponse> eemExperimentSubscriptionsSubscriptionIdGet(@ApiParam(value = "",required=true) @PathVariable("subscriptionId") String subscriptionId) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<ExperimentExecutionSubscriptionResponse>(objectMapper.readValue("{\n  \"id\" : \"beb7074c-845e-4170-be2e-7e33e31e921a\",\n  \"callbackURI\" : \"http://127.0.0.1/subscribe\",\n  \"subscriptionType\" : \"EXPERIMENT_EXECUTION_CHANGE_STATUS\",\n  \"executionId\" : \"experiment_execution_id\"\n}", ExperimentExecutionSubscriptionResponse.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<ExperimentExecutionSubscriptionResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<ExperimentExecutionSubscriptionResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentSubscriptionsSubscriptionIdOptions(@ApiParam(value = "",required=true) @PathVariable("subscriptionId") String subscriptionId) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemOptions() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> listVersionsv1() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

}
