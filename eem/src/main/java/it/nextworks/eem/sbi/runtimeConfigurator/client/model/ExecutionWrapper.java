/*
 * OpenAPI definition
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: v0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package it.nextworks.eem.sbi.runtimeConfigurator.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
/**
 * ExecutionWrapper
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2020-05-29T12:19:50.083Z[GMT]")
public class ExecutionWrapper {
  @SerializedName("execScript")
  private String executionScript = null;

  public ExecutionWrapper executionScript(String executionScript) {
    this.executionScript = executionScript;
    return this;
  }

   /**
   * Get executionScript
   * @return executionScript
  **/
  public String getExecutionScript() {
    return executionScript;
  }

  public void setExecutionScript(String executionScript) {
    this.executionScript = executionScript;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExecutionWrapper executionWrapper = (ExecutionWrapper) o;
    return Objects.equals(this.executionScript, executionWrapper.executionScript);
  }

  @Override
  public int hashCode() {
    return Objects.hash(executionScript);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExecutionWrapper {\n");
    
    sb.append("    executionScript: ").append(toIndentedString(executionScript)).append("\n");
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
