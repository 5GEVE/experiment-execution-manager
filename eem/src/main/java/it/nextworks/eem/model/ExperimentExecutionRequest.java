package it.nextworks.eem.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * ExperimentExecutionRequest
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-03T08:24:25.833Z[GMT]")
public class ExperimentExecutionRequest   {
  @JsonProperty("nsInstanceId")
  private String nsInstanceId = null;

  @JsonProperty("experimentDescriptorId")
  private String experimentDescriptorId = null;

  @JsonProperty("executionId")
  private String executionId = null;

  @JsonProperty("testCaseDescriptorConfiguration")
  private TestCaseDescrConfigMap testCaseDescriptorConfiguration = null;

  public ExperimentExecutionRequest nsInstanceId(String nsInstanceId) {
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

  public ExperimentExecutionRequest experimentDescriptorId(String experimentDescriptorId) {
    this.experimentDescriptorId = experimentDescriptorId;
    return this;
  }

  /**
   * Get experimentDescriptorId
   * @return experimentDescriptorId
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    public String getExperimentDescriptorId() {
    return experimentDescriptorId;
  }

  public void setExperimentDescriptorId(String experimentDescriptorId) {
    this.experimentDescriptorId = experimentDescriptorId;
  }

  public ExperimentExecutionRequest executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  /**
   * Get executionId
   * @return executionId
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public ExperimentExecutionRequest testCaseDescriptorConfiguration(TestCaseDescrConfigMap testCaseDescriptorConfiguration) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExperimentExecutionRequest experimentExecutionRequest = (ExperimentExecutionRequest) o;
    return Objects.equals(this.nsInstanceId, experimentExecutionRequest.nsInstanceId) &&
        Objects.equals(this.experimentDescriptorId, experimentExecutionRequest.experimentDescriptorId) &&
        Objects.equals(this.executionId, experimentExecutionRequest.executionId) &&
        Objects.equals(this.testCaseDescriptorConfiguration, experimentExecutionRequest.testCaseDescriptorConfiguration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nsInstanceId, experimentDescriptorId, executionId, testCaseDescriptorConfiguration);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExperimentExecutionRequest {\n");
    
    sb.append("    nsInstanceId: ").append(toIndentedString(nsInstanceId)).append("\n");
    sb.append("    experimentDescriptorId: ").append(toIndentedString(experimentDescriptorId)).append("\n");
    sb.append("    executionId: ").append(toIndentedString(executionId)).append("\n");
    sb.append("    testCaseDescriptorConfiguration: ").append(toIndentedString(testCaseDescriptorConfiguration)).append("\n");
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

  @JsonIgnore
  public void isValid() throws MalformattedElementException {
    if(executionId == null)
      throw new MalformattedElementException("executionId cannot be null");
    if(nsInstanceId == null)
      throw new MalformattedElementException("nsInstanceId cannot be null");
    if(experimentDescriptorId == null)
      throw new MalformattedElementException("experimentDescriptorId cannot be null");
  }
}
