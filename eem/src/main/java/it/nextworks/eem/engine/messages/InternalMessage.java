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
package it.nextworks.eem.engine.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "msgType")
@JsonSubTypes({
		@Type(value = RunAllExperimentInternalMessage.class, name = "RUN_ALL_EXPERIMENT"),
		@Type(value = RunStepExperimentInternalMessage.class, name = "RUN_STEP_EXPERIMENT"),
		@Type(value = RunStepExperimentInternalMessage.class, name = "ABORT_EXPERIMENT"),
		@Type(value = RunStepExperimentInternalMessage.class, name = "PAUSE_EXPERIMENT"),
		@Type(value = RunStepExperimentInternalMessage.class, name = "STEP_EXPERIMENT"),
		@Type(value = RunStepExperimentInternalMessage.class, name = "RESUME_STEP_EXPERIMENT"),
})
public abstract class InternalMessage {
	
	@JsonProperty("type")
	InternalMessageType type;

	/**
	 * @return the type
	 */
	public InternalMessageType getType() {
		return type;
	}

	
}
