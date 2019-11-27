package it.nextworks.eem.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.nextworks.eem.model.TestCaseResult;

import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * ExperimentExecution
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-27T13:24:37.065Z[GMT]")
public class ExperimentExecution   {
  @JsonProperty("executionId")
  private String executionId = null;

  /**
   * Status of the experiment
   */
  public enum StatusEnum {
    INIT("INIT"),
    
    CONFIGURING("CONFIGURING"),
    
    RUNNING("RUNNING"),
    
    TERMINATED("TERMINATED"),
    
    ABORTING("ABORTING"),
    
    ABORTED("ABORTED"),
    
    FAILED("FAILED");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String text) {
      for (StatusEnum b : StatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("status")
  private StatusEnum status = null;

  @JsonProperty("testCaseResult")
  private TestCaseResult testCaseResult = null;

  @JsonProperty("reportUrl")
  private String reportUrl = null;

  @JsonProperty("eemSubscriptionId")
  private String eemSubscriptionId = null;

  public ExperimentExecution executionId(String executionId) {
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

  public ExperimentExecution status(StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Status of the experiment
   * @return status
  **/
  @ApiModelProperty(required = true, value = "Status of the experiment")
      @NotNull

    public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public ExperimentExecution testCaseResult(TestCaseResult testCaseResult) {
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

  public ExperimentExecution reportUrl(String reportUrl) {
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

  public ExperimentExecution eemSubscriptionId(String eemSubscriptionId) {
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
    ExperimentExecution experimentExecution = (ExperimentExecution) o;
    return Objects.equals(this.executionId, experimentExecution.executionId) &&
        Objects.equals(this.status, experimentExecution.status) &&
        Objects.equals(this.testCaseResult, experimentExecution.testCaseResult) &&
        Objects.equals(this.reportUrl, experimentExecution.reportUrl) &&
        Objects.equals(this.eemSubscriptionId, experimentExecution.eemSubscriptionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(executionId, status, testCaseResult, reportUrl, eemSubscriptionId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExperimentExecution {\n");
    
    sb.append("    executionId: ").append(toIndentedString(executionId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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
