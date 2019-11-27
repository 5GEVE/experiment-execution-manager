package it.nextworks.eem.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * ExperimentChangeStateNotification
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-27T13:24:37.065Z[GMT]")
public class ExperimentChangeStateNotification   {
  @JsonProperty("operationState")
  private String operationState = null;

  @JsonProperty("experimentExecutionId")
  private String experimentExecutionId = null;

  public ExperimentChangeStateNotification operationState(String operationState) {
    this.operationState = operationState;
    return this;
  }

  /**
   * Get operationState
   * @return operationState
  **/
  @ApiModelProperty(value = "")
  
    public String getOperationState() {
    return operationState;
  }

  public void setOperationState(String operationState) {
    this.operationState = operationState;
  }

  public ExperimentChangeStateNotification experimentExecutionId(String experimentExecutionId) {
    this.experimentExecutionId = experimentExecutionId;
    return this;
  }

  /**
   * Get experimentExecutionId
   * @return experimentExecutionId
  **/
  @ApiModelProperty(value = "")
  
    public String getExperimentExecutionId() {
    return experimentExecutionId;
  }

  public void setExperimentExecutionId(String experimentExecutionId) {
    this.experimentExecutionId = experimentExecutionId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExperimentChangeStateNotification experimentChangeStateNotification = (ExperimentChangeStateNotification) o;
    return Objects.equals(this.operationState, experimentChangeStateNotification.operationState) &&
        Objects.equals(this.experimentExecutionId, experimentChangeStateNotification.experimentExecutionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(operationState, experimentExecutionId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExperimentChangeStateNotification {\n");
    
    sb.append("    operationState: ").append(toIndentedString(operationState)).append("\n");
    sb.append("    experimentExecutionId: ").append(toIndentedString(experimentExecutionId)).append("\n");
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
