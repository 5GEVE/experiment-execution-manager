package it.nextworks.eem.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * ExperimentExecutionResponse
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-03T08:24:25.833Z[GMT]")
public class ExperimentExecutionResponse   {
  @JsonProperty("executionId")
  private String executionId = null;

  @JsonProperty("state")
  private ExperimentState state = null;

  @JsonProperty("testCaseResult")
  private TestCaseResult testCaseResult = null;

  @JsonProperty("reportUrl")
  private String reportUrl = null;

  @JsonProperty("eemSubscriptionId")
  private String eemSubscriptionId = null;

  public ExperimentExecutionResponse executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  /**
   * Identifier of the executed experiment
   * @return executionId
  **/
  @ApiModelProperty(required = true, value = "Identifier of the executed experiment")
      @NotNull

    public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public ExperimentExecutionResponse state(ExperimentState state) {
    this.state = state;
    return this;
  }

  /**
   * Get state
   * @return state
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    @Valid
    public ExperimentState getState() {
    return state;
  }

  public void setState(ExperimentState state) {
    this.state = state;
  }

  public ExperimentExecutionResponse testCaseResult(TestCaseResult testCaseResult) {
    this.testCaseResult = testCaseResult;
    return this;
  }

  /**
   * Get testCaseResult
   * @return testCaseResult
  **/
  @ApiModelProperty(value = "")
  
    @Valid
    public TestCaseResult getTestCaseResult() {
    return testCaseResult;
  }

  public void setTestCaseResult(TestCaseResult testCaseResult) {
    this.testCaseResult = testCaseResult;
  }

  public ExperimentExecutionResponse reportUrl(String reportUrl) {
    this.reportUrl = reportUrl;
    return this;
  }

  /**
   * URL containing the report of the execution
   * @return reportUrl
  **/
  @ApiModelProperty(value = "URL containing the report of the execution")
  
    public String getReportUrl() {
    return reportUrl;
  }

  public void setReportUrl(String reportUrl) {
    this.reportUrl = reportUrl;
  }

  public ExperimentExecutionResponse eemSubscriptionId(String eemSubscriptionId) {
    this.eemSubscriptionId = eemSubscriptionId;
    return this;
  }

  /**
   * Get eemSubscriptionId
   * @return eemSubscriptionId
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    public String getEemSubscriptionId() {
    return eemSubscriptionId;
  }

  public void setEemSubscriptionId(String eemSubscriptionId) {
    this.eemSubscriptionId = eemSubscriptionId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExperimentExecutionResponse experimentExecutionResponse = (ExperimentExecutionResponse) o;
    return Objects.equals(this.executionId, experimentExecutionResponse.executionId) &&
        Objects.equals(this.state, experimentExecutionResponse.state) &&
        Objects.equals(this.testCaseResult, experimentExecutionResponse.testCaseResult) &&
        Objects.equals(this.reportUrl, experimentExecutionResponse.reportUrl) &&
        Objects.equals(this.eemSubscriptionId, experimentExecutionResponse.eemSubscriptionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(executionId, state, testCaseResult, reportUrl, eemSubscriptionId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExperimentExecutionResponse {\n");
    
    sb.append("    executionId: ").append(toIndentedString(executionId)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    testCaseResult: ").append(toIndentedString(testCaseResult)).append("\n");
    sb.append("    reportUrl: ").append(toIndentedString(reportUrl)).append("\n");
    sb.append("    eemSubscriptionId: ").append(toIndentedString(eemSubscriptionId)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
