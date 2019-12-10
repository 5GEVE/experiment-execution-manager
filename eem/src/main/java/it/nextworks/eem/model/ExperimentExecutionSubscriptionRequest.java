package it.nextworks.eem.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.nextworks.eem.model.enumerates.SubscriptionType;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.*;

/**
 * Subsription reuqest to an execution experiment
 */
@ApiModel(description = "Subsription reuqest to an execution experiment")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-03T08:24:25.833Z[GMT]")
public class ExperimentExecutionSubscriptionRequest   {

  @JsonProperty("subscriptionType")
  private SubscriptionType subscriptionType = null;

  @JsonProperty("callbackURI")
  private String callbackURI = null;

  @JsonProperty("executionId")
  private String executionId = null;

  public ExperimentExecutionSubscriptionRequest subscriptionType(SubscriptionType subscriptionType) {
    this.subscriptionType = subscriptionType;
    return this;
  }

  /**
   * Get subscriptionType
   * @return subscriptionType
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    public SubscriptionType getSubscriptionType() {
    return subscriptionType;
  }

  public void setSubscriptionType(SubscriptionType subscriptionType) {
    this.subscriptionType = subscriptionType;
  }

  public ExperimentExecutionSubscriptionRequest callbackURI(String callbackURI) {
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

  public ExperimentExecutionSubscriptionRequest executionId(String executionId) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExperimentExecutionSubscriptionRequest experimentExecutionSubscriptionRequest = (ExperimentExecutionSubscriptionRequest) o;
    return Objects.equals(this.subscriptionType, experimentExecutionSubscriptionRequest.subscriptionType) &&
        Objects.equals(this.callbackURI, experimentExecutionSubscriptionRequest.callbackURI) &&
        Objects.equals(this.executionId, experimentExecutionSubscriptionRequest.executionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subscriptionType, callbackURI, executionId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExperimentExecutionSubscriptionRequest {\n");
    
    sb.append("    subscriptionType: ").append(toIndentedString(subscriptionType)).append("\n");
    sb.append("    callbackURI: ").append(toIndentedString(callbackURI)).append("\n");
    sb.append("    executionId: ").append(toIndentedString(executionId)).append("\n");
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
