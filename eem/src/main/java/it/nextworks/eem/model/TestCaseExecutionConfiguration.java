package it.nextworks.eem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity
public class TestCaseExecutionConfiguration {

	@Id
    @GeneratedValue
    @JsonIgnore
    private Long id;
	
	@JsonIgnore
	@ManyToOne
	private ExperimentExecution execution;
	
	private String tcDescriptorId;

	//the value of the map is a map with the user parameters to be overwritten for the given run
	//the format of the map in the value field is the same as in the test case descriptor: 
	//Key: parameter name, as in the key of the corresponding map in the blueprint; value: desired value
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@ElementCollection(fetch= FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private Map<String, String> execConfiguration = new HashMap<String, String>();
	
	public TestCaseExecutionConfiguration() { }

	/**
	 * @param tcDescriptorId
	 * @param execConfiguration
	 */
	public TestCaseExecutionConfiguration(ExperimentExecution execution, String tcDescriptorId, Map<String, String> execConfiguration) {
		this.execution = execution;
		this.tcDescriptorId = tcDescriptorId;
		if (execConfiguration != null) this.execConfiguration = execConfiguration;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the tcDescriptorId
	 */
	public String getTcDescriptorId() {
		return tcDescriptorId;
	}

	public void setTcDescriptorId(String tcDescriptorId) {
		this.tcDescriptorId = tcDescriptorId;
	}

	public TestCaseExecutionConfiguration tcDescriptorId(String tcDescriptorId) {
		this.tcDescriptorId = tcDescriptorId;
		return this;
	}

	/**
	 * @return the execConfiguration
	 */
	public Map<String, String> getExecConfiguration() {
		return execConfiguration;
	}

	public void setExecConfiguration(Map<String, String> execConfiguration) {
		this.execConfiguration = execConfiguration;
	}

	public TestCaseExecutionConfiguration execConfiguration(Map<String, String> execConfiguration) {
		this.execConfiguration = execConfiguration;
		return this;
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
		TestCaseExecutionConfiguration testCaseExecutionConfiguration = (TestCaseExecutionConfiguration) o;
		return Objects.equals(this.id, testCaseExecutionConfiguration.id) &&
				Objects.equals(this.tcDescriptorId, testCaseExecutionConfiguration.tcDescriptorId) &&
				Objects.equals(this.execConfiguration, testCaseExecutionConfiguration.execConfiguration);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, tcDescriptorId , execConfiguration);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class TestCaseExecutionConfiguration {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    tcDescriptorId: ").append(toIndentedString(tcDescriptorId)).append("\n");
		sb.append("    execConfiguration: ").append(toIndentedString(execConfiguration)).append("\n");
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
