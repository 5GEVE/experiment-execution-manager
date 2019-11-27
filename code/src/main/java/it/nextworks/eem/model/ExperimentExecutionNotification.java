package it.nextworks.eem.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * ExperimentExecutionNotification
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-27T13:24:37.065Z[GMT]")
public class ExperimentExecutionNotification   {
  @JsonProperty("id")
  private String id = null;

  /**
   * Status of the notification. Should be \"Configuring\" till configuration ends. Then, if configuration is performed, the status is CONFIGURED, otherwise is \"FAILED\"
   */
  public enum NotificationStateEnum {
    CONFIGURING("CONFIGURING"),
    
    CONFIGURED("CONFIGURED"),
    
    FAILED("FAILED");

    private String value;

    NotificationStateEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static NotificationStateEnum fromValue(String text) {
      for (NotificationStateEnum b : NotificationStateEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("notificationState")
  private NotificationStateEnum notificationState = null;

  @JsonProperty("subscriptionId")
  private String subscriptionId = null;

  @JsonProperty("experimentExecutionId")
  private String experimentExecutionId = null;

  @JsonProperty("timestamp")
  private BigDecimal timestamp = null;

  public ExperimentExecutionNotification id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Notification ID
   * @return id
  **/
  @ApiModelProperty(value = "Notification ID")
  
    public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ExperimentExecutionNotification notificationState(NotificationStateEnum notificationState) {
    this.notificationState = notificationState;
    return this;
  }

  /**
   * Status of the notification. Should be \"Configuring\" till configuration ends. Then, if configuration is performed, the status is CONFIGURED, otherwise is \"FAILED\"
   * @return notificationState
  **/
  @ApiModelProperty(required = true, value = "Status of the notification. Should be \"Configuring\" till configuration ends. Then, if configuration is performed, the status is CONFIGURED, otherwise is \"FAILED\"")
      @NotNull

    public NotificationStateEnum getNotificationState() {
    return notificationState;
  }

  public void setNotificationState(NotificationStateEnum notificationState) {
    this.notificationState = notificationState;
  }

  public ExperimentExecutionNotification subscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
    return this;
  }

  /**
   * Identifier of the subscription that has triggered that notification.
   * @return subscriptionId
  **/
  @ApiModelProperty(value = "Identifier of the subscription that has triggered that notification.")
  
    public String getSubscriptionId() {
    return subscriptionId;
  }

  public void setSubscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
  }

  public ExperimentExecutionNotification experimentExecutionId(String experimentExecutionId) {
    this.experimentExecutionId = experimentExecutionId;
    return this;
  }

  /**
   * Identifier of the experiment execution
   * @return experimentExecutionId
  **/
  @ApiModelProperty(value = "Identifier of the experiment execution")
  
    public String getExperimentExecutionId() {
    return experimentExecutionId;
  }

  public void setExperimentExecutionId(String experimentExecutionId) {
    this.experimentExecutionId = experimentExecutionId;
  }

  public ExperimentExecutionNotification timestamp(BigDecimal timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  /**
   * Get timestamp
   * @return timestamp
  **/
  @ApiModelProperty(value = "")
  
    @Valid
    public BigDecimal getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(BigDecimal timestamp) {
    this.timestamp = timestamp;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExperimentExecutionNotification experimentExecutionNotification = (ExperimentExecutionNotification) o;
    return Objects.equals(this.id, experimentExecutionNotification.id) &&
        Objects.equals(this.notificationState, experimentExecutionNotification.notificationState) &&
        Objects.equals(this.subscriptionId, experimentExecutionNotification.subscriptionId) &&
        Objects.equals(this.experimentExecutionId, experimentExecutionNotification.experimentExecutionId) &&
        Objects.equals(this.timestamp, experimentExecutionNotification.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, notificationState, subscriptionId, experimentExecutionId, timestamp);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExperimentExecutionNotification {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    notificationState: ").append(toIndentedString(notificationState)).append("\n");
    sb.append("    subscriptionId: ").append(toIndentedString(subscriptionId)).append("\n");
    sb.append("    experimentExecutionId: ").append(toIndentedString(experimentExecutionId)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
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
