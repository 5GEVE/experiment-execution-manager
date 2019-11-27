package it.nextworks.eem.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import it.nextworks.eem.model.ConfigurationChangeNotification;
import it.nextworks.eem.model.ErrorInfo;
import it.nextworks.eem.model.ExperimentExecution;
import it.nextworks.eem.model.ExperimentExecutionInfo;
import it.nextworks.eem.model.ExperimentExecutionResponse;
import it.nextworks.eem.model.ExperimentExecutionSubscription;

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
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-27T13:24:37.065Z[GMT]")
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

    public ResponseEntity<List<ExperimentExecutionInfo>> eemExperimentsExecutionGet(@ApiParam(value = "Execution state of the experiment", allowableValues = "INIT, CONFIGURING, RUNNING, RUNNING_STEP, PAUSED, VALIDATING, COMPLETED, ABORTING, ABORTED, FAILED") @Valid @RequestParam(value = "status", required = false) String status) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<List<ExperimentExecutionInfo>>(objectMapper.readValue("[ {\n  \"jobId\" : [ \"jobId\", \"jobId\" ],\n  \"nsInstanceId\" : \"nsInstanceId\",\n  \"experimentDescriptorId\" : \"experimentDescriptorId\",\n  \"id\" : \"id\",\n  \"testCaseDescriptorConfiguration\" : \"\",\n  \"experimentReportURL\" : \"experimentReportURL\",\n  \"status\" : \"INIT\"\n}, {\n  \"jobId\" : [ \"jobId\", \"jobId\" ],\n  \"nsInstanceId\" : \"nsInstanceId\",\n  \"experimentDescriptorId\" : \"experimentDescriptorId\",\n  \"id\" : \"id\",\n  \"testCaseDescriptorConfiguration\" : \"\",\n  \"experimentReportURL\" : \"experimentReportURL\",\n  \"status\" : \"INIT\"\n} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<List<ExperimentExecutionInfo>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<List<ExperimentExecutionInfo>>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentsExecutionIdAbortPost(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentsExecutionIdDelete(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<ExperimentExecution> eemExperimentsExecutionIdGet(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<ExperimentExecution>(objectMapper.readValue("{\n  \"executionId\" : \"executionId\",\n  \"testCaseResult\" : \"\",\n  \"reportUrl\" : \"reportUrl\",\n  \"eemSubscriptionId\" : \"eemSubscriptionId\",\n  \"status\" : \"INIT\"\n}", ExperimentExecution.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<ExperimentExecution>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<ExperimentExecution>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentsExecutionIdOptions(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentsExecutionIdPausePost(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentsExecutionIdRunPost(@ApiParam(value = "",required=true) @PathVariable("id") String id,@ApiParam(value = "Determine the type of run. If not present, the default value is RUN_ALL" , allowableValues="RUN_IN_STEPS, RUN_ALL") @RequestHeader(value="runType", required=false) String runType) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentsExecutionIdStepPost(@ApiParam(value = "",required=true) @PathVariable("id") String id) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemExperimentsExecutionOptions() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<ExperimentExecutionResponse> eemExperimentsExecutionPost() {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<ExperimentExecutionResponse>(objectMapper.readValue("{\n  \"metadata\" : \"metadata\",\n  \"experimentExecutionInfo\" : {\n    \"jobId\" : [ \"jobId\", \"jobId\" ],\n    \"nsInstanceId\" : \"nsInstanceId\",\n    \"experimentDescriptorId\" : \"experimentDescriptorId\",\n    \"id\" : \"id\",\n    \"testCaseDescriptorConfiguration\" : \"\",\n    \"experimentReportURL\" : \"experimentReportURL\",\n    \"status\" : \"INIT\"\n  }\n}", ExperimentExecutionResponse.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<ExperimentExecutionResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<ExperimentExecutionResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemNotificationsGet() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemNotificationsOptions() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemNotificationsPost(@ApiParam(value = "" ,required=true )  @Valid @RequestBody ConfigurationChangeNotification body) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemOptions() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<List<ExperimentExecutionSubscription>> eemSubscriptionsGet() {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<List<ExperimentExecutionSubscription>>(objectMapper.readValue("[ {\n  \"subscriptionType\" : \"EXPERIMENT_EXECUTION_CHANGE_STATUS\",\n  \"experimentExecutionId\" : \"experiment_execution_id\"\n}, {\n  \"subscriptionType\" : \"EXPERIMENT_EXECUTION_CHANGE_STATUS\",\n  \"experimentExecutionId\" : \"experiment_execution_id\"\n} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<List<ExperimentExecutionSubscription>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<List<ExperimentExecutionSubscription>>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemSubscriptionsOptions() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<ExperimentExecutionSubscription> eemSubscriptionsPost(@ApiParam(value = "" ,required=true )  @Valid @RequestBody ExperimentExecutionSubscription body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<ExperimentExecutionSubscription>(objectMapper.readValue("{\n  \"subscriptionType\" : \"EXPERIMENT_EXECUTION_CHANGE_STATUS\",\n  \"experimentExecutionId\" : \"experiment_execution_id\"\n}", ExperimentExecutionSubscription.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<ExperimentExecutionSubscription>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<ExperimentExecutionSubscription>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemSubscriptionsSubscriptionIdDelete(@ApiParam(value = "",required=true) @PathVariable("subscriptionId") String subscriptionId) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<ExperimentExecutionSubscription> eemSubscriptionsSubscriptionIdGet(@ApiParam(value = "",required=true) @PathVariable("subscriptionId") String subscriptionId) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<ExperimentExecutionSubscription>(objectMapper.readValue("{\n  \"subscriptionType\" : \"EXPERIMENT_EXECUTION_CHANGE_STATUS\",\n  \"experimentExecutionId\" : \"experiment_execution_id\"\n}", ExperimentExecutionSubscription.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<ExperimentExecutionSubscription>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<ExperimentExecutionSubscription>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> eemSubscriptionsSubscriptionIdOptions(@ApiParam(value = "",required=true) @PathVariable("subscriptionId") String subscriptionId) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Void> listVersionsv1() {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

}
