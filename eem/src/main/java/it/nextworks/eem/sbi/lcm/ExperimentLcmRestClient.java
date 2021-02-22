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
package it.nextworks.eem.sbi.lcm;

import it.nextworks.eem.model.ExperimentExecutionStateChangeNotification;
import it.nextworks.eem.sbi.RequestResponseLoggingInterceptor;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import java.util.Collections;


public class ExperimentLcmRestClient {

	private static final Logger log = LoggerFactory.getLogger(ExperimentLcmRestClient.class);

	private RestTemplate restTemplate;

	public ExperimentLcmRestClient() {
	    this.restTemplate= new RestTemplate(new BufferingClientHttpRequestFactory(
				new SimpleClientHttpRequestFactory()
		));
	    this.restTemplate.setInterceptors(Collections.singletonList(new RequestResponseLoggingInterceptor()));
	}

	public void notifyExperimentExecutionStateChange(String url, ExperimentExecutionStateChangeNotification msg) throws FailedOperationException {
		log.debug("Building HTTP request for notifying Experiment Execution with Id {} state change in {}", msg.getExperimentExecutionId(), msg.getCurrentStatus());
		HttpHeaders header = new HttpHeaders();
		header.add("Content-Type", "application/json");
		HttpEntity<?> entity = new HttpEntity<>(msg, header);
		try {
			log.debug("Sending HTTP request");
			ResponseEntity<?> httpResponse =
					restTemplate.exchange(url, HttpMethod.POST, entity, ExperimentExecutionStateChangeNotification.class);
			HttpStatus code = httpResponse.getStatusCode();
			if (code.equals(HttpStatus.OK))
				log.debug("Experiment Execution state change notification sent correctly");
		} catch (HttpClientErrorException e) {
			throw new FailedOperationException("EEM: Error sending Experiment Execution state change notification to Lcm: Client error", e);
		} catch (HttpServerErrorException e) {
			throw new FailedOperationException("LCM: Error sending Experiment Execution state change notification to Lcm: Server error", e);
		} catch (UnknownHttpStatusCodeException e) {
			throw new FailedOperationException("EEM: Error sending Experiment Execution state change notification to Lcm: Unknown error", e);
		}catch (Exception e){
			throw new FailedOperationException("EEM: Generic Error while interacting with Experiment Lcm", e);
		}
	}
}
