package it.nextworks.eem.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import it.nextworks.eem.model.ExperimentExecutionInfo;
import it.nextworks.eem.model.NSInfo;

import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * ExperimentExecutionRequest
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-27T13:24:37.065Z[GMT]")
public class ExperimentExecutionRequest   {
  @JsonProperty("experimentExecutionInfo")
  private ExperimentExecutionInfo experimentExecutionInfo = null;

  @JsonProperty("nsInfo")
  private NSInfo nsInfo = null;

  public ExperimentExecutionRequest experimentExecutionInfo(ExperimentExecutionInfo experimentExecutionInfo) {
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

  public ExperimentExecutionRequest nsInfo(NSInfo nsInfo) {
    this.nsInfo = nsInfo;
    return this;
  }

  /**
   * Get nsInfo
   * @return nsInfo
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    @Valid
    public NSInfo getNsInfo() {
    return nsInfo;
  }

  public void setNsInfo(NSInfo nsInfo) {
    this.nsInfo = nsInfo;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExperimentExecutionRequest experimentExecutionRequest = (ExperimentExecutionRequest) o;
    return Objects.equals(this.experimentExecutionInfo, experimentExecutionRequest.experimentExecutionInfo) &&
        Objects.equals(this.nsInfo, experimentExecutionRequest.nsInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(experimentExecutionInfo, nsInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExperimentExecutionRequest {\n");
    
    sb.append("    experimentExecutionInfo: ").append(toIndentedString(experimentExecutionInfo)).append("\n");
    sb.append("    nsInfo: ").append(toIndentedString(nsInfo)).append("\n");
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
