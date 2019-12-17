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

public class TestCaseResultInternalMessage extends InternalMessage {

	@JsonProperty("tcDescriptorId")
	private String tcDescriptorId;

	@JsonProperty("failed")
	private boolean failed;

	@JsonProperty("result")
	private String result;

	//TODO add reportUrl?

	@JsonCreator
	public TestCaseResultInternalMessage(@JsonProperty("result") String result, @JsonProperty("tcDescriptorId") String tcDescriptorId, @JsonProperty("failed") boolean failed) {
		this.type = InternalMessageType.TC_RESULT;
		this.result = result;
		this.tcDescriptorId = tcDescriptorId;
		this.failed = failed;
	}

	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * @return the tcDescriptorId
	 */
	public String getTcDescriptorId() { return tcDescriptorId; }

	/**
	 * @return failed
	 */
	public boolean isFailed() { return failed; }
}
