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
 * Subsription response
 */
@ApiModel(description = "Subsription response")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-29T09:22:19.643Z[GMT]")
public class ExperimentExecutionSubscriptionResponse   {
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

  @JsonProperty("executionId")
  private String executionId = null;

  @JsonProperty("callbackURI")
  private String callbackURI = null;

  @JsonProperty("id")
  private String id = null;

  public ExperimentExecutionSubscriptionResponse subscriptionType(SubscriptionTypeEnum subscriptionType) {
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

  public ExperimentExecutionSubscriptionResponse executionId(String executionId) {
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

  public ExperimentExecutionSubscriptionResponse callbackURI(String callbackURI) {
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

  public ExperimentExecutionSubscriptionResponse id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Subscribtion ID
   * @return id
  **/
  @ApiModelProperty(required = true, value = "Subscribtion ID")
      @NotNull

    public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExperimentExecutionSubscriptionResponse experimentExecutionSubscriptionResponse = (ExperimentExecutionSubscriptionResponse) o;
    return Objects.equals(this.subscriptionType, experimentExecutionSubscriptionResponse.subscriptionType) &&
        Objects.equals(this.executionId, experimentExecutionSubscriptionResponse.executionId) &&
        Objects.equals(this.callbackURI, experimentExecutionSubscriptionResponse.callbackURI) &&
        Objects.equals(this.id, experimentExecutionSubscriptionResponse.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subscriptionType, executionId, callbackURI, id);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExperimentExecutionSubscriptionResponse {\n");
    
    sb.append("    subscriptionType: ").append(toIndentedString(subscriptionType)).append("\n");
    sb.append("    executionId: ").append(toIndentedString(executionId)).append("\n");
    sb.append("    callbackURI: ").append(toIndentedString(callbackURI)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
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
