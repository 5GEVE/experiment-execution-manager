package it.nextworks.eem.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import org.springframework.validation.annotation.Validated;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.*;

/**
 * ExecutionResult
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-03T08:24:25.833Z[GMT]")
@Entity
public class ExecutionResult   {

  @Id
  @GeneratedValue
  @JsonIgnore
  private Long id;

  @JsonIgnore
  @ManyToOne
  private ExperimentExecution execution;

  @JsonProperty("result")
  private String result = null;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ExecutionResult result(String result) {
    this.result = result;
    return this;
  }

  /**
   * Get result
   * @return result
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public ExperimentExecution getExecution() {
    return execution;
  }

  public void setExecution(ExperimentExecution execution) {
    this.execution = execution;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExecutionResult executionResult = (ExecutionResult) o;
    return Objects.equals(this.id, executionResult.id) &&
            Objects.equals(this.result, executionResult.result);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, result);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExecutionResult {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    result: ").append(toIndentedString(result)).append("\n");
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
    if(result == null)
      throw new MalformattedElementException("result cannot be null");
  }
}
