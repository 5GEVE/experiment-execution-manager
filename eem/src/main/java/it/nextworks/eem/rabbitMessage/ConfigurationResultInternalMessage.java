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
package it.nextworks.eem.rabbitMessage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.eem.model.ConfigurationStatus;
import it.nextworks.eem.model.ValidationStatus;

import java.util.List;

public class ConfigurationResultInternalMessage extends InternalMessage {

	@JsonProperty("failed")
	private boolean failed;

	@JsonProperty("result")
	private String result;

	@JsonProperty("metricConfigIds")
	private List<String> metricConfigIds;

	@JsonProperty("configurationStatus")
	private ConfigurationStatus configurationStatus;

	@JsonCreator
	public ConfigurationResultInternalMessage(@JsonProperty("configurationStatus") ConfigurationStatus configurationStatus, @JsonProperty("result") String result, @JsonProperty("metricConfigIds") List<String> metricConfigIds, @JsonProperty("failed") boolean failed) {
		this.type = InternalMessageType.CONFIGURATION_RESULT;
		this.result = result;
		this.failed = failed;
		this.configurationStatus = configurationStatus;
		this.metricConfigIds = metricConfigIds;
	}

	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * @return failed
	 */
	public boolean isFailed() { return failed; }

	/**
	 * @return configurationStatus
	 */
	public ConfigurationStatus getConfigurationStatus() {
		return configurationStatus;
	}

	/**
	 * @return metricConfigIds
	 */
	public List<String> getMetricConfigIds() {
		return metricConfigIds;
	}
}
