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
import it.nextworks.eem.sbi.expcatalogue.ExperimentCatalogueRestClient;
import it.nextworks.nfvmano.catalogue.blueprint.elements.ExpBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.ExpBlueprintInfo;
import it.nextworks.nfvmano.catalogue.blueprint.messages.*;
import it.nextworks.nfvmano.catalogue.translator.NfvNsInstantiationInfo;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.*;
import it.nextworks.nfvmano.libs.ifa.common.messages.GeneralizedQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
public class SbiExperimentLcmService {

	private static final Logger log = LoggerFactory.getLogger(SbiExperimentLcmService.class);

	private ExperimentLcmRestClient experimentLcmRestClient;

	public SbiExperimentLcmService() {}

	@PostConstruct
	private void initLcmRestClient() {
		log.debug("Initializing Experiment LifeCycle Manager REST client");
		experimentLcmRestClient = new ExperimentLcmRestClient();
	}

	public void notifyExperimentExecutionStateChange(String url, ExperimentExecutionStateChangeNotification msg) {
		try {
			experimentLcmRestClient.notifyExperimentExecutionStateChange(url, msg);
		} catch (FailedOperationException e) {
			log.error(e.getMessage());
			log.debug(null, e);
			//TODO handle error?
		}
 	}
}
