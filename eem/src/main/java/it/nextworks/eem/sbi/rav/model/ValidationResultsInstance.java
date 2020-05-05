/*
 * RAV API
 * RAV API
 *
 * OpenAPI spec version: 1.0.0-oas3
 * Contact: name@mail.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package it.nextworks.eem.sbi.rav.model;

import java.util.Objects;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
/**
 * ValidationResultsInstance
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2020-04-06T19:10:49.373Z[GMT]")
public class ValidationResultsInstance {
  @SerializedName("vertical")
  private String vertical = null;

  @SerializedName("expID")
  private String expID = null;

  @SerializedName("testcases")
  private List<ConfigurationDictTestcases> testcases = null;

  @SerializedName("added")
  private String added = null;

  @SerializedName("finished")
  private String finished = null;

  @SerializedName("status")
  private String status = null;

  @SerializedName("validation")
  private ValidationResults validation = null;

  public ValidationResultsInstance vertical(String vertical) {
    this.vertical = vertical;
    return this;
  }

   /**
   * Get vertical
   * @return vertical
  **/
  public String getVertical() {
    return vertical;
  }

  public void setVertical(String vertical) {
    this.vertical = vertical;
  }

  public ValidationResultsInstance expID(String expID) {
    this.expID = expID;
    return this;
  }

   /**
   * Get expID
   * @return expID
  **/
  public String getExpID() {
    return expID;
  }

  public void setExpID(String expID) {
    this.expID = expID;
  }

  public ValidationResultsInstance testcases(List<ConfigurationDictTestcases> testcases) {
    this.testcases = testcases;
    return this;
  }

  public ValidationResultsInstance addTestcasesItem(ConfigurationDictTestcases testcasesItem) {
    if (this.testcases == null) {
      this.testcases = new ArrayList<ConfigurationDictTestcases>();
    }
    this.testcases.add(testcasesItem);
    return this;
  }

   /**
   * Get testcases
   * @return testcases
  **/
  public List<ConfigurationDictTestcases> getTestcases() {
    return testcases;
  }

  public void setTestcases(List<ConfigurationDictTestcases> testcases) {
    this.testcases = testcases;
  }

  public ValidationResultsInstance added(String added) {
    this.added = added;
    return this;
  }

   /**
   * Get added
   * @return added
  **/
  public String getAdded() {
    return added;
  }

  public void setAdded(String added) {
    this.added = added;
  }

  public ValidationResultsInstance finished(String finished) {
    this.finished = finished;
    return this;
  }

   /**
   * Get finished
   * @return finished
  **/
  public String getFinished() {
    return finished;
  }

  public void setFinished(String finished) {
    this.finished = finished;
  }

  public ValidationResultsInstance status(String status) {
    this.status = status;
    return this;
  }

   /**
   * Get status
   * @return status
  **/
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public ValidationResultsInstance validation(ValidationResults validation) {
    this.validation = validation;
    return this;
  }

   /**
   * Get validation
   * @return validation
  **/
  public ValidationResults getValidation() {
    return validation;
  }

  public void setValidation(ValidationResults validation) {
    this.validation = validation;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ValidationResultsInstance validationResultsInstance = (ValidationResultsInstance) o;
    return Objects.equals(this.vertical, validationResultsInstance.vertical) &&
        Objects.equals(this.expID, validationResultsInstance.expID) &&
        Objects.equals(this.testcases, validationResultsInstance.testcases) &&
        Objects.equals(this.added, validationResultsInstance.added) &&
        Objects.equals(this.finished, validationResultsInstance.finished) &&
        Objects.equals(this.status, validationResultsInstance.status) &&
        Objects.equals(this.validation, validationResultsInstance.validation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(vertical, expID, testcases, added, finished, status, validation);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ValidationResultsInstance {\n");
    
    sb.append("    vertical: ").append(toIndentedString(vertical)).append("\n");
    sb.append("    expID: ").append(toIndentedString(expID)).append("\n");
    sb.append("    testcases: ").append(toIndentedString(testcases)).append("\n");
    sb.append("    added: ").append(toIndentedString(added)).append("\n");
    sb.append("    finished: ").append(toIndentedString(finished)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    validation: ").append(toIndentedString(validation)).append("\n");
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