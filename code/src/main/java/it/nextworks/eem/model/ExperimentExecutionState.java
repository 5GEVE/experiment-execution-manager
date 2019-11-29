package it.nextworks.eem.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * ExperimentExecutionState
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-28T08:59:03.567Z[GMT]")
public class ExperimentExecutionState   {
  /**
   * Gets or Sets notificationType
   */
  public enum NotificationTypeEnum {
    EXPERIMENTEXECUTIONSTATECHANGE("ExperimentExecutionStateChange");

    private String value;

    NotificationTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static NotificationTypeEnum fromValue(String text) {
      for (NotificationTypeEnum b : NotificationTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("notificationType")
  private NotificationTypeEnum notificationType = null;

  /**
   * Gets or Sets operationState
   */
  public enum OperationStateEnum {
    RUNNING("RUNNING"),
    
    FAILED("FAILED"),
    
    COMPLETED("COMPLETED");

    private String value;

    OperationStateEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static OperationStateEnum fromValue(String text) {
      for (OperationStateEnum b : OperationStateEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("operationState")
  private OperationStateEnum operationState = null;

  @JsonProperty("experimentExecutionId")
  private String experimentExecutionId = null;

  public ExperimentExecutionState notificationType(NotificationTypeEnum notificationType) {
    this.notificationType = notificationType;
    return this;
  }

  /**
   * Get notificationType
   * @return notificationType
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    public NotificationTypeEnum getNotificationType() {
    return notificationType;
  }

  public void setNotificationType(NotificationTypeEnum notificationType) {
    this.notificationType = notificationType;
  }

  public ExperimentExecutionState operationState(OperationStateEnum operationState) {
    this.operationState = operationState;
    return this;
  }

  /**
   * Get operationState
   * @return operationState
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    public OperationStateEnum getOperationState() {
    return operationState;
  }

  public void setOperationState(OperationStateEnum operationState) {
    this.operationState = operationState;
  }

  public ExperimentExecutionState experimentExecutionId(String experimentExecutionId) {
    this.experimentExecutionId = experimentExecutionId;
    return this;
  }

  /**
   * Get experimentExecutionId
   * @return experimentExecutionId
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

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
    ExperimentExecutionState experimentExecutionState = (ExperimentExecutionState) o;
    return Objects.equals(this.notificationType, experimentExecutionState.notificationType) &&
        Objects.equals(this.operationState, experimentExecutionState.operationState) &&
        Objects.equals(this.experimentExecutionId, experimentExecutionState.experimentExecutionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(notificationType, operationState, experimentExecutionId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExperimentExecutionState {\n");
    
    sb.append("    notificationType: ").append(toIndentedString(notificationType)).append("\n");
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
