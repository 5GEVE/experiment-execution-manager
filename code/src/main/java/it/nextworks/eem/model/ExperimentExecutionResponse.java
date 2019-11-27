package it.nextworks.eem.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.nextworks.eem.model.ExperimentExecutionInfo;

import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * ExperimentExecutionResponse
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-27T13:24:37.065Z[GMT]")
public class ExperimentExecutionResponse   {
  @JsonProperty("metadata")
  private String metadata = null;

  @JsonProperty("experimentExecutionInfo")
  private ExperimentExecutionInfo experimentExecutionInfo = null;

  public ExperimentExecutionResponse metadata(String metadata) {
    this.metadata = metadata;
    return this;
  }

  /**
   * Additional paramenters to be added
   * @return metadata
  **/
  @ApiModelProperty(value = "Additional paramenters to be added")
  
    public String getMetadata() {
    return metadata;
  }

  public void setMetadata(String metadata) {
    this.metadata = metadata;
  }

  public ExperimentExecutionResponse experimentExecutionInfo(ExperimentExecutionInfo experimentExecutionInfo) {
    this.experimentExecutionInfo = experimentExecutionInfo;
    return this;
  }

  /**
   * Get experimentExecutionInfo
   * @return experimentExecutionInfo
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    @Valid
    public ExperimentExecutionInfo getExperimentExecutionInfo() {
    return experimentExecutionInfo;
  }

  public void setExperimentExecutionInfo(ExperimentExecutionInfo experimentExecutionInfo) {
    this.experimentExecutionInfo = experimentExecutionInfo;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExperimentExecutionResponse experimentExecutionResponse = (ExperimentExecutionResponse) o;
    return Objects.equals(this.metadata, experimentExecutionResponse.metadata) &&
        Objects.equals(this.experimentExecutionInfo, experimentExecutionResponse.experimentExecutionInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(metadata, experimentExecutionInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExperimentExecutionResponse {\n");
    
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("    experimentExecutionInfo: ").append(toIndentedString(experimentExecutionInfo)).append("\n");
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
