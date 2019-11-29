package it.nextworks.eem.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.nextworks.eem.model.ExperimentState;

import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * ExperimentExecutionNotification
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-29T09:22:19.643Z[GMT]")
public class ExperimentExecutionNotification   {
  @JsonProperty("executionId")
  private String executionId = null;

  @JsonProperty("state")
  private ExperimentState state = null;

  @JsonProperty("subscriptionId")
  private String subscriptionId = null;

  @JsonProperty("callbackURI")
  private String callbackURI = null;

  public ExperimentExecutionNotification executionId(String executionId) {
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

  public ExperimentExecutionNotification state(ExperimentState state) {
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

  public ExperimentExecutionNotification subscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
    return this;
  }

  /**
   * Get subscriptionId
   * @return subscriptionId
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    public String getSubscriptionId() {
    return subscriptionId;
  }

  public void setSubscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
  }

  public ExperimentExecutionNotification callbackURI(String callbackURI) {
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
    ExperimentExecutionNotification experimentExecutionNotification = (ExperimentExecutionNotification) o;
    return Objects.equals(this.executionId, experimentExecutionNotification.executionId) &&
        Objects.equals(this.state, experimentExecutionNotification.state) &&
        Objects.equals(this.subscriptionId, experimentExecutionNotification.subscriptionId) &&
        Objects.equals(this.callbackURI, experimentExecutionNotification.callbackURI);
  }

  @Override
  public int hashCode() {
    return Objects.hash(executionId, state, subscriptionId, callbackURI);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExperimentExecutionNotification {\n");
    
    sb.append("    executionId: ").append(toIndentedString(executionId)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    subscriptionId: ").append(toIndentedString(subscriptionId)).append("\n");
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
