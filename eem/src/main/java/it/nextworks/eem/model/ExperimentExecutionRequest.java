package it.nextworks.eem.model;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.validation.annotation.Validated;

import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * ExperimentExecutionRequest
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-03T08:24:25.833Z[GMT]")
public class ExperimentExecutionRequest   {
  @JsonProperty("nsInstanceId")
  private String nsInstanceId = null;

  @JsonProperty("experimentDescriptorId")
  private String experimentDescriptorId = null;

  @JsonProperty("testCaseDescriptorConfiguration")
  private TestCaseDescrConfigMap testCaseDescriptorConfiguration = null;

  @JsonProperty("tenantId")
  private String tenantId;

  @JsonProperty("siteNames")
  private List<String> siteNames = new ArrayList<>();

  @JsonProperty("experimentId")
  private String experimentId;

  @JsonProperty("useCase")
  private String useCase;

  @JsonProperty("infrastructureMetrics")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @ElementCollection(fetch = FetchType.EAGER)
  @Fetch(FetchMode.SELECT)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
  private Map<String, String> infrastructureMetrics = new HashMap<>();

  @JsonProperty("applicationMetrics")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @ElementCollection(fetch = FetchType.EAGER)
  @Fetch(FetchMode.SELECT)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
  private Map<String, String> applicationMetrics = new HashMap<>();

  @JsonProperty("kpiMetrics")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @ElementCollection(fetch = FetchType.EAGER)
  @Fetch(FetchMode.SELECT)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
  private Map<String, String> kpiMetrics = new HashMap<>();


  public ExperimentExecutionRequest useCase(String useCase){
    this.useCase = useCase;
    return this;
  }

  public ExperimentExecutionRequest nsInstanceId(String nsInstanceId) {
    this.nsInstanceId = nsInstanceId;
    return this;
  }

  public ExperimentExecutionRequest tenantId(String tenantId){
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Get nsInstanceId
   * @return nsInstanceId
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    public String getNsInstanceId() {
    return nsInstanceId;
  }

  public void setNsInstanceId(String nsInstanceId) {
    this.nsInstanceId = nsInstanceId;
  }

  public ExperimentExecutionRequest experimentDescriptorId(String experimentDescriptorId) {
    this.experimentDescriptorId = experimentDescriptorId;
    return this;
  }

  public ExperimentExecutionRequest siteNames(List<String> siteNames) {
    if (this.siteNames != null){
      for (String site: siteNames)
        this.siteNames.add(site);
    }
    return this;
  }


  public ExperimentExecutionRequest experimentId(String experimentId){
    this.experimentId = experimentId;
    return this;
  }
  /**
   * Get experimentDescriptorId
   * @return experimentDescriptorId
  **/
  @ApiModelProperty(required = true, value = "")
      @NotNull

    public String getExperimentDescriptorId() {
    return experimentDescriptorId;
  }

  public void setExperimentDescriptorId(String experimentDescriptorId) {
    this.experimentDescriptorId = experimentDescriptorId;
  }

  public ExperimentExecutionRequest testCaseDescriptorConfiguration(TestCaseDescrConfigMap testCaseDescriptorConfiguration) {
    this.testCaseDescriptorConfiguration = testCaseDescriptorConfiguration;
    return this;
  }

  public ExperimentExecutionRequest applicationMetrics(Map<String, String> applicationMetrics){
    this.applicationMetrics = applicationMetrics;
    return this; 
  }


  public ExperimentExecutionRequest kpiMetrics(Map<String, String> kpiMetrics){
    this.kpiMetrics = kpiMetrics;
    return this; 
  }


  public ExperimentExecutionRequest infrastructureMetrics(Map<String, String> infrastructureMetrics){
    this.infrastructureMetrics = infrastructureMetrics;
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

  /**
   * Get testCaseDescriptorConfiguration
   * @return testCaseDescriptorConfiguration
  **/
  @ApiModelProperty(value = "")
  
    @Valid
    public TestCaseDescrConfigMap getTestCaseDescriptorConfiguration() {
    return testCaseDescriptorConfiguration;
  }

  public void setTestCaseDescriptorConfiguration(TestCaseDescrConfigMap testCaseDescriptorConfiguration) {
    this.testCaseDescriptorConfiguration = testCaseDescriptorConfiguration;
  }

  public String getTenantId(){
    return this.tenantId;
  }

  public void setTenantId(String tenantId){
    this.tenantId = tenantId;
  }

  public List<String> getSiteNames() {
    return siteNames;
  }

  public void setSiteNames(List<String> siteNames) {
    if (siteNames != null) {
      for (String site : siteNames)
        this.siteNames.add(site);
    }
  }


  public String getUseCase() {
    return useCase;
  }

  public void setUseCase(String useCase) {
    this.useCase = useCase;
  }

  public String getExperimentId() {
    return experimentId;
  }

  public void setExperimentId(String experimentId) {
    this.experimentId = experimentId;
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
    return Objects.equals(this.nsInstanceId, experimentExecutionRequest.nsInstanceId) &&
        Objects.equals(this.experimentDescriptorId, experimentExecutionRequest.experimentDescriptorId) &&
        Objects.equals(this.testCaseDescriptorConfiguration, experimentExecutionRequest.testCaseDescriptorConfiguration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nsInstanceId, experimentDescriptorId, testCaseDescriptorConfiguration, tenantId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExperimentExecutionRequest {\n");
    
    sb.append("    nsInstanceId: ").append(toIndentedString(nsInstanceId)).append("\n");
    sb.append("    experimentDescriptorId: ").append(toIndentedString(experimentDescriptorId)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    testCaseDescriptorConfiguration: ").append(toIndentedString(testCaseDescriptorConfiguration)).append("\n");
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
    if(experimentDescriptorId == null)
      throw new MalformattedElementException("experimentDescriptorId cannot be null");
  }
}
