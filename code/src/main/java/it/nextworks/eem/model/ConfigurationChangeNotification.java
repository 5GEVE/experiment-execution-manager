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
 * ConfigurationChangeNotification
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-29T09:22:19.643Z[GMT]")
public class ConfigurationChangeNotification   {
  @JsonProperty("executionId")
  private String executionId = null;

  /**
   * Gets or Sets configurationChangeState
   */
  public enum ConfigurationChangeStateEnum {
    CONFIGURING("CONFIGURING"),
    
    CONFIGURED("CONFIGURED"),
    
    CONFIGURATION_FAILED("CONFIGURATION_FAILED");

    private String value;

    ConfigurationChangeStateEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ConfigurationChangeStateEnum fromValue(String text) {
      for (ConfigurationChangeStateEnum b : ConfigurationChangeStateEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("configurationChangeState")
  private ConfigurationChangeStateEnum configurationChangeState = null;

  public ConfigurationChangeNotification executionId(String executionId) {
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

  public ConfigurationChangeNotification configurationChangeState(ConfigurationChangeStateEnum configurationChangeState) {
    this.configurationChangeState = configurationChangeState;
    return this;
  }

  /**
   * Get configurationChangeState
   * @return configurationChangeState
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    public ConfigurationChangeStateEnum getConfigurationChangeState() {
    return configurationChangeState;
  }

  public void setConfigurationChangeState(ConfigurationChangeStateEnum configurationChangeState) {
    this.configurationChangeState = configurationChangeState;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConfigurationChangeNotification configurationChangeNotification = (ConfigurationChangeNotification) o;
    return Objects.equals(this.executionId, configurationChangeNotification.executionId) &&
        Objects.equals(this.configurationChangeState, configurationChangeNotification.configurationChangeState);
  }

  @Override
  public int hashCode() {
    return Objects.hash(executionId, configurationChangeState);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConfigurationChangeNotification {\n");
    
    sb.append("    executionId: ").append(toIndentedString(executionId)).append("\n");
    sb.append("    configurationChangeState: ").append(toIndentedString(configurationChangeState)).append("\n");
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
