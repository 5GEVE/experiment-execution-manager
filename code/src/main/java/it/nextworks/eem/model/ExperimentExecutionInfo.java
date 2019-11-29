package it.nextworks.eem.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.nextworks.eem.model.TestCaseDescrConfigMap;

import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Experiment Execution Information
 */
@ApiModel(description = "Experiment Execution Information")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-28T08:59:03.567Z[GMT]")
public class ExperimentExecutionInfo   {
  @JsonProperty("id")
  private String id = null;

  /**
   * Experiment state. It identifies the status of the job. Should be \"CREATED\" when the EEM receives the creation request.
   */
  public enum StatusEnum {
    INIT("INIT"),
    
    CONFIGURING("CONFIGURING"),
    
    RUNNING("RUNNING"),
    
    RUNNING_STEP("RUNNING_STEP"),
    
    PAUSED("PAUSED"),
    
    VALIDATING("VALIDATING"),
    
    COMPLETED("COMPLETED"),
    
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

  @JsonProperty("jobId")
  @Valid
  private List<String> jobId = null;

  @JsonProperty("experimentReportURL")
  private String experimentReportURL = null;

  @JsonProperty("testCaseDescriptorConfiguration")
  private TestCaseDescrConfigMap testCaseDescriptorConfiguration = null;

  @JsonProperty("nsInstanceId")
  private String nsInstanceId = null;

  @JsonProperty("experimentDescriptorId")
  private String experimentDescriptorId = null;

  public ExperimentExecutionInfo id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Experiment Execution Id as generated from Experiment Execution Manager
   * @return id
  **/
  @ApiModelProperty(required = true, value = "Experiment Execution Id as generated from Experiment Execution Manager")
      @NotNull

    public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ExperimentExecutionInfo status(StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Experiment state. It identifies the status of the job. Should be \"CREATED\" when the EEM receives the creation request.
   * @return status
  **/
  @ApiModelProperty(required = true, value = "Experiment state. It identifies the status of the job. Should be \"CREATED\" when the EEM receives the creation request.")
      @NotNull

    public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public ExperimentExecutionInfo jobId(List<String> jobId) {
    this.jobId = jobId;
    return this;
  }

  public ExperimentExecutionInfo addJobIdItem(String jobIdItem) {
    if (this.jobId == null) {
      this.jobId = new ArrayList<String>();
    }
    this.jobId.add(jobIdItem);
    return this;
  }

  /**
   * Identifies the job name on the Jenkins system
   * @return jobId
  **/
  @ApiModelProperty(value = "Identifies the job name on the Jenkins system")
  
    public List<String> getJobId() {
    return jobId;
  }

  public void setJobId(List<String> jobId) {
    this.jobId = jobId;
  }

  public ExperimentExecutionInfo experimentReportURL(String experimentReportURL) {
    this.experimentReportURL = experimentReportURL;
    return this;
  }

  /**
   * Experiment Report URL to retrieve validation results
   * @return experimentReportURL
  **/
  @ApiModelProperty(value = "Experiment Report URL to retrieve validation results")
  
    public String getExperimentReportURL() {
    return experimentReportURL;
  }

  public void setExperimentReportURL(String experimentReportURL) {
    this.experimentReportURL = experimentReportURL;
  }

  public ExperimentExecutionInfo testCaseDescriptorConfiguration(TestCaseDescrConfigMap testCaseDescriptorConfiguration) {
    this.testCaseDescriptorConfiguration = testCaseDescriptorConfiguration;
    return this;
  }

  /**
   * Get testCaseDescriptorConfiguration
   * @return testCaseDescriptorConfiguration
  **/
  @ApiModelProperty(value = "")
  
    @Valid
    public TestCaseDescrConfigMap getTestCaseDescriptorConfiguration() {
    return testCaseDescriptorConfiguration;
  }

  public void setTestCaseDescriptorConfiguration(TestCaseDescrConfigMap testCaseDescriptorConfiguration) {
    this.testCaseDescriptorConfiguration = testCaseDescriptorConfiguration;
  }

  public ExperimentExecutionInfo nsInstanceId(String nsInstanceId) {
    this.nsInstanceId = nsInstanceId;
    return this;
  }

  /**
   * Get nsInstanceId
   * @return nsInstanceId
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    public String getNsInstanceId() {
    return nsInstanceId;
  }

  public void setNsInstanceId(String nsInstanceId) {
    this.nsInstanceId = nsInstanceId;
  }

  public ExperimentExecutionInfo experimentDescriptorId(String experimentDescriptorId) {
    this.experimentDescriptorId = experimentDescriptorId;
    return this;
  }

  /**
   * Experiment Description Id as received from Experiment Lifecycle Manager
   * @return experimentDescriptorId
  **/
  @ApiModelProperty(required = true, value = "Experiment Description Id as received from Experiment Lifecycle Manager")
      @NotNull

    public String getExperimentDescriptorId() {
    return experimentDescriptorId;
  }

  public void setExperimentDescriptorId(String experimentDescriptorId) {
    this.experimentDescriptorId = experimentDescriptorId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExperimentExecutionInfo experimentExecutionInfo = (ExperimentExecutionInfo) o;
    return Objects.equals(this.id, experimentExecutionInfo.id) &&
        Objects.equals(this.status, experimentExecutionInfo.status) &&
        Objects.equals(this.jobId, experimentExecutionInfo.jobId) &&
        Objects.equals(this.experimentReportURL, experimentExecutionInfo.experimentReportURL) &&
        Objects.equals(this.testCaseDescriptorConfiguration, experimentExecutionInfo.testCaseDescriptorConfiguration) &&
        Objects.equals(this.nsInstanceId, experimentExecutionInfo.nsInstanceId) &&
        Objects.equals(this.experimentDescriptorId, experimentExecutionInfo.experimentDescriptorId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, status, jobId, experimentReportURL, testCaseDescriptorConfiguration, nsInstanceId, experimentDescriptorId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExperimentExecutionInfo {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    jobId: ").append(toIndentedString(jobId)).append("\n");
    sb.append("    experimentReportURL: ").append(toIndentedString(experimentReportURL)).append("\n");
    sb.append("    testCaseDescriptorConfiguration: ").append(toIndentedString(testCaseDescriptorConfiguration)).append("\n");
    sb.append("    nsInstanceId: ").append(toIndentedString(nsInstanceId)).append("\n");
    sb.append("    experimentDescriptorId: ").append(toIndentedString(experimentDescriptorId)).append("\n");
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
