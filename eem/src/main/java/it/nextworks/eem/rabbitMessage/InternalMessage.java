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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "msgType")
@JsonSubTypes({
		@Type(value = RunAllExperimentInternalMessage.class, name = "RUN_ALL"),
		@Type(value = RunStepExperimentInternalMessage.class, name = "RUN_STEP"),
		@Type(value = AbortExperimentInternalMessage.class, name = "ABORT"),
		@Type(value = PauseExperimentInternalMessage.class, name = "PAUSE"),
		@Type(value = StepExperimentInternalMessage.class, name = "STEP"),
		@Type(value = ResumeExperimentInternalMessage.class, name = "RESUME"),
		@Type(value = TestCaseResultInternalMessage.class, name = "TC_RESULT"),
		@Type(value = ValidationResultInternalMessage.class, name = "VALIDATION_RESULT"),
		@Type(value = ConfigurationResultInternalMessage.class, name = "CONFIGURATION_RESULT")
})
public abstract class InternalMessage {
	
	@JsonProperty("msgType")
	InternalMessageType type;

	/**
	 * @return the type
	 */
	public InternalMessageType getType() {
		return type;
	}

	
}
