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
import it.nextworks.nfvmano.catalogue.blueprint.elements.TestCaseBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.TestCaseDescriptor;

public class RunTestCaseInternalMessage extends InternalMessage {

	@JsonProperty("tcDescriptorId")
	private String tcDescriptorId;

	@JsonProperty("robotFile")
	private String robotFile; //TODO change type

	@JsonCreator
	public RunTestCaseInternalMessage(@JsonProperty("tcDescriptorId") String tcDescriptorId, @JsonProperty("robotFile") String robotFile) {
		this.type = InternalMessageType.RUN_TEST_CASE;
		this.tcDescriptorId = tcDescriptorId;
		this.robotFile = robotFile;
	}

	/**
	 * @return the tcDescriptorId
	 */
	public String getTcDescriptorId() {
		return tcDescriptorId;
	}

	/**
	 * @return the robotFile
	 */
	public String getRobotFile() { return robotFile; }
}
