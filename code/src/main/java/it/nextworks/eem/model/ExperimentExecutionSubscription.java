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
 * ExperimentExecutionSubscription
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-27T13:24:37.065Z[GMT]")
public class ExperimentExecutionSubscription   {
  /**
   * Gets or Sets subscriptionType
   */
  public enum SubscriptionTypeEnum {
    STATE("EXPERIMENT_EXECUTION_CHANGE_STATE");

    private String value;

    SubscriptionTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static SubscriptionTypeEnum fromValue(String text) {
      for (SubscriptionTypeEnum b : SubscriptionTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("subscriptionType")
  private SubscriptionTypeEnum subscriptionType = null;

  @JsonProperty("experimentExecutionId")
  private String experimentExecutionId = null;

  @JsonProperty("callbackURI")
  private String callbackURI = null;

  public ExperimentExecutionSubscription subscriptionType(SubscriptionTypeEnum subscriptionType) {
    this.subscriptionType = subscriptionType;
    return this;
  }

  /**
   * Get subscriptionType
   * @return subscriptionType
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    public SubscriptionTypeEnum getSubscriptionType() {
    return subscriptionType;
  }

  public void setSubscriptionType(SubscriptionTypeEnum subscriptionType) {
    this.subscriptionType = subscriptionType;
  }

  public ExperimentExecutionSubscription experimentExecutionId(String experimentExecutionId) {
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

  public ExperimentExecutionSubscription callbackURI(String callbackURI) {
    this.callbackURI = callbackURI;
    return this;
  }

  /**
   * Get callbackURI
   * @return callbackURI
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    public String getCallbackURI() {
    return callbackURI;
  }

  public void setCallbackURI(String callbackURI) {
    this.callbackURI = callbackURI;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExperimentExecutionSubscription experimentExecutionSubscription = (ExperimentExecutionSubscription) o;
    return Objects.equals(this.subscriptionType, experimentExecutionSubscription.subscriptionType) &&
        Objects.equals(this.experimentExecutionId, experimentExecutionSubscription.experimentExecutionId) &&
        Objects.equals(this.callbackURI, experimentExecutionSubscription.callbackURI);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subscriptionType, experimentExecutionId, callbackURI);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExperimentExecutionSubscription {\n");
    
    sb.append("    subscriptionType: ").append(toIndentedString(subscriptionType)).append("\n");
    sb.append("    experimentExecutionId: ").append(toIndentedString(experimentExecutionId)).append("\n");
    sb.append("    callbackURI: ").append(toIndentedString(callbackURI)).append("\n");
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
