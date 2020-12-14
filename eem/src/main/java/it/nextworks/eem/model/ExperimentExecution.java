package it.nextworks.eem.model;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.nextworks.eem.model.enumerate.ExperimentRunType;
import it.nextworks.eem.model.enumerate.ExperimentState;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import org.hibernate.annotations.*;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * ExperimentExecutionResponse
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-03T08:24:25.833Z[GMT]")
@Entity
public class ExperimentExecution {

  @Id
  @GeneratedValue
  @JsonIgnore
  private Long id;

  @JsonProperty("executionId")
  private String executionId = null;

  @JsonProperty("state")
  private ExperimentState state = null;

  @JsonProperty("nsInstanceId")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String nsInstanceId = null;

  @JsonProperty("experimentDescriptorId")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String experimentDescriptorId = null;

  //this list specifies the test cases for the requested execution//TODO how to run a subset of test cases?
  //in general a subset of the test cases can be executed in each run and the config parameters of the descriptors can be overwritten
  //Important note: this field is optional, i.e. if not provided all the test cases will be executed by default, with the configuration given in the descriptor
  @JsonProperty("testCaseDescriptorConfiguration")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @OneToMany(mappedBy = "execution", cascade= CascadeType.ALL, orphanRemoval = true)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @LazyCollection(LazyCollectionOption.FALSE)
  private List<TestCaseExecutionConfiguration> testCaseDescriptorConfiguration = new ArrayList<>();

  @JsonProperty("testCaseResult")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @OneToMany(mappedBy = "execution", cascade= CascadeType.ALL, orphanRemoval = true)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @LazyCollection(LazyCollectionOption.FALSE)
  private Map<String, ExecutionResult> testCaseResult = new HashMap<>();

  @JsonProperty("reportUrl")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String reportUrl = null;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonProperty("errorMessage")
  private String errorMessage;

  @JsonIgnore
  @JsonProperty("tenantId")
  private String tenantId = "";

  @JsonIgnore
  @ElementCollection(fetch = FetchType.EAGER)
  @Fetch(FetchMode.SELECT)
  @Cascade(org.hibernate.annotations.CascadeType.ALL)
  @JsonProperty("siteNames")
  private List<String> siteNames = new ArrayList<>();

  @JsonIgnore
  @JsonProperty("experimentId")
  private String experimentId;

  @JsonIgnore
  @JsonProperty("useCase")
  private String useCase = "";

  @JsonProperty("infrastructureMetrics")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @ElementCollection
  private Map<String, String> infrastructureMetrics = new HashMap<>();

  @JsonProperty("applicationMetrics")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @ElementCollection
  private Map<String, String> applicationMetrics = new HashMap<>();

  @JsonProperty("kpiMetrics")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @ElementCollection
  private Map<String, String> kpiMetrics = new HashMap<>();

  @JsonIgnore
  private ExperimentRunType runType;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ExperimentExecution executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public String getUseCase() {
    return useCase;
  }

  public void setUseCase(String useCase) {
    this.useCase = useCase;
  }

  public ExperimentExecution useCase(String useCase){
    this.useCase = useCase;
    return this;
  }

  /**
   * Identifier of the executed experiment
   * @return executionId
  **/
  @ApiModelProperty(required = true, value = "Identifier of the executed experiment")
      @NotNull

    public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public ExperimentExecution state(ExperimentState state) {
    this.state = state;
    return this;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId){
    this.tenantId = tenantId;
  }

  public ExperimentExecution tenantId(String tenantId){
    this.tenantId = tenantId;
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

  public ExperimentExecution nsInstanceId(String nsInstanceId) {
    this.nsInstanceId = nsInstanceId;
    return this;
  }

  /**
   * Get nsInstanceId
   * @return nsInstanceId
   **/
  @ApiModelProperty(required = false, value = "")

  public String getNsInstanceId() {
    return nsInstanceId;
  }

  public void setNsInstanceId(String nsInstanceId) {
    this.nsInstanceId = nsInstanceId;
  }

  public ExperimentExecution experimentDescriptorId(String experimentDescriptorId) {
    this.experimentDescriptorId = experimentDescriptorId;
    return this;
  }

  /**
   * Get experimentDescriptorId
   * @return experimentDescriptorId
   **/
  @ApiModelProperty(required = false, value = "")

  public String getExperimentDescriptorId() {
    return experimentDescriptorId;
  }

  public void setExperimentDescriptorId(String experimentDescriptorId) {
    this.experimentDescriptorId = experimentDescriptorId;
  }

  public ExperimentExecution testCaseResult(Map<String, ExecutionResult> testCaseResult) {
    this.testCaseResult = testCaseResult;
    for (ExecutionResult er : this.testCaseResult.values()) {
      er.setExecution(this);
    }
    return this;
  }

  /**
   * Get testCaseResult
   * @return testCaseResult
  **/
  @ApiModelProperty(value = "")

  public Map<String, ExecutionResult> getTestCaseResult() {
    return testCaseResult;
  }

  public void setTestCaseResult(Map<String, ExecutionResult> testCaseResult) {
    this.testCaseResult = testCaseResult;
    for (ExecutionResult er : this.testCaseResult.values()) {
      er.setExecution(this);
    }
  }

  public void addTestCaseResult(String testCaseId, ExecutionResult result){
    result.setExecution(this);
    this.testCaseResult.put(testCaseId, result);
  }

  public ExperimentExecution reportUrl(String reportUrl) {
    this.reportUrl = reportUrl;
    return this;
  }

  /**
   * URL containing the report of the execution
   * @return reportUrl
  **/
  @ApiModelProperty(value = "URL containing the report of the execution")

    public String getReportUrl() {
    return reportUrl;
  }

  public void setReportUrl(String reportUrl) {
    this.reportUrl = reportUrl;
  }

  public ExperimentExecution testCaseDescriptorConfiguration(List<TestCaseExecutionConfiguration> testCaseDescriptorConfiguration) {
    this.testCaseDescriptorConfiguration = testCaseDescriptorConfiguration;
    for (TestCaseExecutionConfiguration tc : this.testCaseDescriptorConfiguration) {
      tc.setExecution(this);
    }
    return this;
  }

  /**
   * Test cases for the requested execution
   * @return testCaseDescriptorConfiguration
   **/
  @ApiModelProperty(value = "Test cases for the requested execution")

  public List<TestCaseExecutionConfiguration> getTestCaseDescriptorConfiguration() { return this.testCaseDescriptorConfiguration; }

  public void setTestCaseDescriptorConfiguration(List<TestCaseExecutionConfiguration> testCaseDescriptorConfiguration) {
    this.testCaseDescriptorConfiguration = testCaseDescriptorConfiguration;
    for (TestCaseExecutionConfiguration tc : this.testCaseDescriptorConfiguration) {
      tc.setExecution(this);
    }
  }

  public ExperimentExecution errorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  /**
   * Error Message
   * @return errorMessage
   **/
  @ApiModelProperty(required = false, value = "Error Message")

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }


  public ExperimentExecution runType(ExperimentRunType runType) {
    this.runType = runType;
    return this;
  }

  public ExperimentRunType getRunType() {
    return runType;
  }

  public void setRunType(ExperimentRunType runType) {
    this.runType = runType;
  }

  public List<String> getSiteNames() {
    return this.siteNames;
  }

  public void setSiteNames(List<String> siteNames) {
    this.siteNames = siteNames;
  }

  public ExperimentExecution siteNames(List<String> siteNames){
    this.siteNames = siteNames;
    return this;
  }
  public String getExperimentId() {
    return experimentId;
  }

  public void setExperimentId(String experimentId) {
    this.experimentId = experimentId;
  }

  public ExperimentExecution experimentId(String experimentId){
    this.experimentId = experimentId;
    return this;
  }

  public Map<String, String> getInfrastructureMetrics() {
    return infrastructureMetrics;
  }

  public void setInfrastructureMetrics(Map<String, String> infrastructureMetrics) {
    this.infrastructureMetrics = infrastructureMetrics;
  }

  public Map<String, String> getApplicationMetrics() {
    return applicationMetrics;
  }

  public void setApplicationMetrics(Map<String, String> applicationMetrics) {
    this.applicationMetrics = applicationMetrics;
  }

  public Map<String, String> getKpiMetrics() {
    return kpiMetrics;
  }

  public void setKpiMetrics(Map<String, String> kpiMetrics) {
    this.kpiMetrics = kpiMetrics;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExperimentExecution experimentExecutionResponse = (ExperimentExecution) o;
    return Objects.equals(this.id, experimentExecutionResponse.id) &&
            Objects.equals(this.executionId, experimentExecutionResponse.executionId) &&
            Objects.equals(this.state, experimentExecutionResponse.state) &&
            Objects.equals(this.testCaseDescriptorConfiguration, experimentExecutionResponse.testCaseDescriptorConfiguration) &&
            Objects.equals(this.testCaseResult, experimentExecutionResponse.testCaseResult) &&
            Objects.equals(this.runType, experimentExecutionResponse.runType) &&
            Objects.equals(this.errorMessage, experimentExecutionResponse.errorMessage) &&
            Objects.equals(this.reportUrl, experimentExecutionResponse.reportUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, executionId, state, testCaseDescriptorConfiguration, testCaseResult, reportUrl, runType, errorMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExperimentExecutionResponse {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    executionId: ").append(toIndentedString(executionId)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    runType: ").append(toIndentedString(runType)).append("\n");
    sb.append("    testCaseResult: ").append(toIndentedString(testCaseResult)).append("\n");
    sb.append("    testCaseDescriptorConfiguration: ").append(toIndentedString(testCaseDescriptorConfiguration)).append("\n");
    sb.append("    reportUrl: ").append(toIndentedString(reportUrl)).append("\n");
    sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
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
    if(state == null)
      throw new MalformattedElementException("state cannot be null");
  }
}
