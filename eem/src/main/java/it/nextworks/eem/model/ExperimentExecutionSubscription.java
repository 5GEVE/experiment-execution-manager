package it.nextworks.eem.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.nextworks.eem.model.enumerate.SubscriptionType;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import org.springframework.validation.annotation.Validated;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.*;

/**
 * Subsription response
 */
@ApiModel(description = "Subsription response")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-03T08:24:25.833Z[GMT]")
@Entity
public class ExperimentExecutionSubscription {

  @Id
  @GeneratedValue
  @JsonIgnore
  private Long id;

  @JsonProperty("subscriptionId")
  private String subscriptionId = null;

  @JsonProperty("subscriptionType")
  private SubscriptionType subscriptionType = null;

  @JsonProperty("executionId")
  private String executionId = null;

  @JsonProperty("callbackURI")
  private String callbackURI = null;

  public ExperimentExecutionSubscription subscriptionType(SubscriptionType subscriptionType) {
    this.subscriptionType = subscriptionType;
    return this;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public ExperimentExecutionSubscription executionId(String executionId) {
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

  public ExperimentExecutionSubscription subscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
    return this;
  }

  /**
   * Subscribtion ID
   * @return id
  **/
  @ApiModelProperty(required = true, value = "Subscribtion ID")
      @NotNull

    public String getSubscriptionId() { return subscriptionId; }

  public void setSubscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
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
    return Objects.equals(this.id, experimentExecutionSubscription.id) &&
        Objects.equals(this.subscriptionId, experimentExecutionSubscription.subscriptionId)    &&
        Objects.equals(this.subscriptionType, experimentExecutionSubscription.subscriptionType) &&
        Objects.equals(this.executionId, experimentExecutionSubscription.executionId) &&
        Objects.equals(this.callbackURI, experimentExecutionSubscription.callbackURI);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, subscriptionType, executionId, callbackURI, subscriptionId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExperimentExecutionSubscriptionResponse {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    subscriptionId: ").append(toIndentedString(subscriptionId)).append("\n");
    sb.append("    subscriptionType: ").append(toIndentedString(subscriptionType)).append("\n");
    sb.append("    executionId: ").append(toIndentedString(executionId)).append("\n");
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

  @JsonIgnore
  public void isValid() throws MalformattedElementException {
    if(executionId == null)
      throw new MalformattedElementException("executionId cannot be null");
    if(subscriptionType == null)
      throw new MalformattedElementException("subscriptionType cannot be null");
    if(callbackURI == null)
      throw new MalformattedElementException("callbackURI cannot be null");
    if(subscriptionId == null)
      throw new MalformattedElementException("subscriptionId cannot be null");
  }
}
