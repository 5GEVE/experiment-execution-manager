/*
* Copyright 2018 Nextworks s.r.l.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package it.nextworks.eem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.eem.model.enumerate.ExperimentState;
import it.nextworks.nfvmano.libs.ifa.common.InterfaceMessage;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;

import java.util.Objects;

public class ExperimentExecutionStateChangeNotification implements InterfaceMessage {

	@JsonProperty("experimentExecutionId")
	private String experimentExecutionId;

	@JsonProperty("currentStatus")
	private ExperimentState currentStatus;
	
	public ExperimentExecutionStateChangeNotification() { }
	
	public ExperimentExecutionStateChangeNotification(String experimentExecutionId, ExperimentState currentStatus) {
		this.experimentExecutionId = experimentExecutionId;
		this.currentStatus = currentStatus;
	}

	/**
	 * @return the experimentExecutionId
	 */
	public String getExperimentExecutionId() {
		return experimentExecutionId;
	}

	/**
	 * @return the currentStatus
	 */
	public ExperimentState getCurrentStatus() {
		return currentStatus;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ExperimentExecutionStateChangeNotification experimentExecutionStateChangeNotification = (ExperimentExecutionStateChangeNotification) o;
		return Objects.equals(this.experimentExecutionId, experimentExecutionStateChangeNotification.experimentExecutionId) &&
				Objects.equals(this.currentStatus, experimentExecutionStateChangeNotification.currentStatus);
	}

	@Override
	public int hashCode() {
		return Objects.hash(experimentExecutionId, currentStatus);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ExperimentExecutionStateChangeNotification {\n");

		sb.append("    experimentExecutionId: ").append(toIndentedString(experimentExecutionId)).append("\n");
		sb.append("    currentStatus: ").append(toIndentedString(currentStatus)).append("\n");
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
	@Override
	public void isValid() throws MalformattedElementException {
		if (experimentExecutionId == null)
			throw new MalformattedElementException("experimentExecutionId cannot be null");
		if(currentStatus == null)
			throw new MalformattedElementException("currentStatus cannot be null");
	}
}
