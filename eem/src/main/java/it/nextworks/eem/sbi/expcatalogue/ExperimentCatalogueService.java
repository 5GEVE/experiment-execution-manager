/*
* Copyright 2019 Nextworks s.r.l.
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
package it.nextworks.eem.sbi.expcatalogue;

import it.nextworks.nfvmano.catalogue.blueprint.elements.ExpBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.ExpBlueprintInfo;
import it.nextworks.nfvmano.catalogue.blueprint.interfaces.*;
import it.nextworks.nfvmano.catalogue.blueprint.messages.*;
import it.nextworks.nfvmano.catalogue.translator.NfvNsInstantiationInfo;
import it.nextworks.nfvmano.catalogue.translator.TranslatorInterface;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.*;
import it.nextworks.nfvmano.libs.ifa.common.messages.GeneralizedQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
public class ExperimentCatalogueService
implements ExpDescriptorCatalogueInterface, ExpBlueprintCatalogueInterface, TranslatorInterface,
	CtxBlueprintCatalogueInterface, CtxDescriptorCatalogueInterface,
	TestCaseBlueprintCatalogueInterface, TestCaseDescriptorCatalogueInterface,
		VsBlueprintCatalogueInterface, VsDescriptorCatalogueInterface {

	private static final Logger log = LoggerFactory.getLogger(ExperimentCatalogueService.class);
	
	@Value("${portal.catalogue.host}")
	private String catalogueHost;

	private ExperimentCatalogueRestClient experimentCatalogueRestClient;
	
	public ExperimentCatalogueService() {}

	public ExpBlueprint getExperimentBlueprint(String expBlueprintId) {
		try {
			QueryExpBlueprintResponse response = experimentCatalogueRestClient.queryExperimentBlueprint(expBlueprintId);
			List<ExpBlueprintInfo> ebis = response.getExpBlueprintInfo();
			if (ebis.isEmpty()) {
				log.error("Experiment blueprint with ID " + expBlueprintId + " not found.");
				return null;
			} else return ebis.get(0).getExpBlueprint();
		} catch (Exception e) {
			log.error("Error while retrieving experiment blueprint", e);
			return null;
		}
 	}

	@Override
	public QueryExpBlueprintResponse queryExpBlueprint(GeneralizedQueryRequest request) throws MalformattedElementException, FailedOperationException {
		log.debug("Received request to query experiment blueprint");
		//The EEM is only allowed to request the experiment blueprint given its ID
		String experimentBlueprintId;
		try {
			experimentBlueprintId = request.getFilter().getParameters().get("ExpB_ID");
		} catch (Exception e) {
			throw new MalformattedElementException("EEM: Malformed Generalized Query Request for retrieving experiment blueprint", e);
		}
		if (experimentBlueprintId == null) throw new MalformattedElementException("EEM: Malformed Generalized Query Request for retrieving experiment blueprint: null ID.");
		return experimentCatalogueRestClient.queryExperimentBlueprint(experimentBlueprintId);
	}
	
	@Override
	public QueryExpDescriptorResponse queryExpDescriptor(GeneralizedQueryRequest request) throws MalformattedElementException, FailedOperationException {
		log.debug("Received request to query experiment descriptor");
        //The EEM is only allowed to request the experiment descriptor given its ID
		String experimentDescriptorId;
		try {
			experimentDescriptorId = request.getFilter().getParameters().get("ExpD_ID");
		} catch (Exception e) {
			throw new MalformattedElementException("EEM: Malformed Generalized Query Request for retrieving experiment descriptor.", e);
		}
		if (experimentDescriptorId == null) throw new MalformattedElementException("EEM: Malformed Generalized Query Request for retrieving experiment descriptor: null ID.");
		return experimentCatalogueRestClient.queryExperimentDescriptor(experimentDescriptorId);
	}
	
	@Override
    public QueryVsBlueprintResponse queryVsBlueprint(GeneralizedQueryRequest request) throws MalformattedElementException, FailedOperationException {
		log.debug("Received request to query vertical service blueprint");
		//The EEM is only allowed to request the vertical service blueprint given its ID
		String blueprintId;
		try {
			blueprintId = request.getFilter().getParameters().get("VSB_ID");
		} catch (Exception e) {
			throw new MalformattedElementException("EEM: Malformed Generalized Query Request for retrieving VS blueprint.", e);
		}
		if (blueprintId == null) throw new MalformattedElementException("EEM: Malformed Generalized Query Request for retrieving VS blueprint: null ID.");
		return experimentCatalogueRestClient.queryVsBlueprint(blueprintId);
	}
	
	@Override
	public QueryVsDescriptorResponse queryVsDescriptor(GeneralizedQueryRequest request) throws MalformattedElementException, FailedOperationException {
		log.debug("Received request to query VS descriptor");
        //The EEM is only allowed to request the VS descriptor given its ID
		String descriptorId;
		try {
			descriptorId = request.getFilter().getParameters().get("VSD_ID");
		} catch (Exception e) {
			throw new MalformattedElementException("EEM: Malformed Generalized Query Request for retrieving VS descriptor.", e);
		}
		if (descriptorId == null) throw new MalformattedElementException("EEM: Malformed Generalized Query Request for retrieving VS descriptor: null ID.");
		return experimentCatalogueRestClient.queryVsDescriptor(descriptorId);
	}
	
	@Override
	public QueryCtxBlueprintResponse queryCtxBlueprint(GeneralizedQueryRequest request) throws MalformattedElementException, FailedOperationException {
		log.debug("Received request to query context blueprint");
		//The EEM is only allowed to request the context blueprint given its ID
		String blueprintId;
		try {
			blueprintId = request.getFilter().getParameters().get("CTXB_ID");
		} catch (Exception e) {
			throw new MalformattedElementException("EEM: Malformed Generalized Query Request for retrieving CTX blueprint.", e);
		}
		if (blueprintId == null) throw new MalformattedElementException("EEM: Malformed Generalized Query Request for retrieving CTX blueprint: null ID.");
		return experimentCatalogueRestClient.queryCtxBlueprint(blueprintId);
	}
	
	@Override
	public QueryCtxDescriptorResponse queryCtxDescriptor(GeneralizedQueryRequest request) throws MalformattedElementException, FailedOperationException {
		log.debug("Received request to query CTXD descriptor");
        //The EEM is only allowed to request the CTX descriptor given its ID
		String descriptorId;
		try {
			descriptorId = request.getFilter().getParameters().get("CTXD_ID");
		} catch (Exception e) {
			throw new MalformattedElementException("EEM: Malformed Generalized Query Request for retrieving CTX descriptor.", e);
		}
		if (descriptorId == null) throw new MalformattedElementException("EEM: Malformed Generalized Query Request for retrieving CTX descriptor: null ID.");
		return experimentCatalogueRestClient.queryCtxDescriptor(descriptorId);
	}
	
	@Override
	public QueryTestCaseBlueprintResponse queryTestCaseBlueprint(GeneralizedQueryRequest request) throws MalformattedElementException, FailedOperationException {
		log.debug("Received request to query TC blueprint");
		//The EEM is only allowed to request the TC blueprint given its ID
		String blueprintId;
		try {
			blueprintId = request.getFilter().getParameters().get("TCB_ID");
		} catch (Exception e) {
			throw new MalformattedElementException("EEM: Malformed Generalized Query Request for retrieving TC blueprint.", e);
		}
		if (blueprintId == null) throw new MalformattedElementException("EEM: Malformed Generalized Query Request for retrieving TC blueprint: null ID.");
		return experimentCatalogueRestClient.queryTestCaseBlueprint(blueprintId);
	}
	
	@Override
	public QueryTestCaseDescriptorResponse queryTestCaseDescriptor(GeneralizedQueryRequest request) throws MalformattedElementException, FailedOperationException {
		log.debug("Received request to query TC descriptor");
        //The EEM is only allowed to request the TC descriptor given its ID
		String descriptorId;
		try {
			descriptorId = request.getFilter().getParameters().get("TCD_ID");
		} catch (Exception e) {
			throw new MalformattedElementException("EEM: Malformed Generalized Query Request for retrieving TC descriptor.", e);
		}
		if (descriptorId == null) throw new MalformattedElementException("EEM: Malformed Generalized Query Request for retrieving TC descriptor: null ID.");
		return experimentCatalogueRestClient.queryTestCaseDescriptor(descriptorId);
	}
	
	@Override
	public NfvNsInstantiationInfo translateExpd(String expdId) throws MalformattedElementException, FailedOperationException {
		log.debug("Received request to translate experiment descriptor into NFV NS specification");
		if (expdId == null) throw new MalformattedElementException("EEM: Malformed Translation Request: null experiment ID.");
		return experimentCatalogueRestClient.translateExpd(expdId);
	}
	
	@Override
    public String onBoardVsBlueprint(OnBoardVsBlueprintRequest request)
            throws MethodNotImplementedException, MalformattedElementException, AlreadyExistingEntityException, FailedOperationException {
		throw new MethodNotImplementedException("The onboarding of a new vertical service blueprint is not allowed through the EEM");
	}

	@Override
    public void deleteVsBlueprint(String vsBlueprintId)
            throws MethodNotImplementedException, MalformattedElementException, NotExistingEntityException, FailedOperationException {
		throw new MethodNotImplementedException("The deletion of a vertical service blueprint is not allowed through the EEM");
	}
	
	@Override
	public String onBoardVsDescriptor(OnboardVsDescriptorRequest request)
			throws MethodNotImplementedException, MalformattedElementException, AlreadyExistingEntityException, FailedOperationException {
		throw new MethodNotImplementedException("The onboarding of a new vertical service descriptor is not allowed through the EEM");
	}
	
	@Override
	public void deleteVsDescriptor(String vsDescriptorId, String tenantId)
			throws MethodNotImplementedException, MalformattedElementException, NotExistingEntityException, FailedOperationException {
		throw new MethodNotImplementedException("The deletion of a vertical service descriptor is not allowed through the EEM");
	}

	@Override
	public String onboardCtxBlueprint(OnboardCtxBlueprintRequest request)
			throws MethodNotImplementedException, MalformattedElementException, AlreadyExistingEntityException, FailedOperationException {
		throw new MethodNotImplementedException("The onboarding of a new context blueprint is not allowed through the EEM");
	}
	
	@Override
	public void deleteCtxBlueprint(String ctxBlueprintId)
			throws MethodNotImplementedException, MalformattedElementException, NotExistingEntityException, FailedOperationException {
		throw new MethodNotImplementedException("The deletion of a context blueprint is not allowed through the EEM");
	}

	@Override
	public String onboardCtxDescriptor(OnboardCtxDescriptorRequest request)
			throws MethodNotImplementedException, MalformattedElementException, AlreadyExistingEntityException, FailedOperationException {
		throw new MethodNotImplementedException("The onboarding of a new context descriptor is not allowed through the EEM");
	}
	
	@Override
	public void deleteCtxDescriptor(String ctxDescriptorId, String tenantId)
			throws MethodNotImplementedException, MalformattedElementException, NotExistingEntityException, FailedOperationException {
		throw new MethodNotImplementedException("The deletion of a context descriptor is not allowed through the EEM");
	}

	@Override
	public String onboardTestCaseBlueprint(OnboardTestCaseBlueprintRequest request)
			throws MethodNotImplementedException, MalformattedElementException, AlreadyExistingEntityException, FailedOperationException {
		throw new MethodNotImplementedException("The onboarding of a new test case blueprint is not allowed through the EEM");
	}
	
	@Override
	public void deleteTestCaseBlueprint(String testCaseBlueprintId)
			throws MethodNotImplementedException, MalformattedElementException, NotExistingEntityException, FailedOperationException {
		throw new MethodNotImplementedException("The deletion of a test case blueprint is not allowed through the EEM");
	}

	@Override
	public String onboardTestCaseDescriptor(OnboardTestCaseDescriptorRequest request)
			throws MethodNotImplementedException, MalformattedElementException, AlreadyExistingEntityException, FailedOperationException {
		throw new MethodNotImplementedException("The onboarding of a new test case descriptor is not allowed through the EEM");
	}
	
	@Override
	public void deleteTestCaseDescriptor(String testcaseDescriptorId, String tenantId)
			throws MethodNotImplementedException, MalformattedElementException, NotExistingEntityException, FailedOperationException {
		throw new MethodNotImplementedException("The deletion of a test case descriptor is not allowed through the EEM");
	}
	
	@Override
	public String onboardExpBlueprint(OnboardExpBlueprintRequest request)
			throws MethodNotImplementedException, MalformattedElementException, AlreadyExistingEntityException,
			FailedOperationException, NotExistingEntityException {
		throw new MethodNotImplementedException("The onboarding of a new experiment blueprint is not allowed through the EEM");
	}

	@Override
	public void deleteExpBlueprint(String expBlueprintId) throws MethodNotImplementedException,
			MalformattedElementException, NotExistingEntityException, FailedOperationException {
		throw new MethodNotImplementedException("The deletion of an experiment blueprint is not allowed through the EEM");
	}
	
	@Override
	public String onboardExpDescriptor(OnboardExpDescriptorRequest request)
			throws MethodNotImplementedException, MalformattedElementException, AlreadyExistingEntityException, FailedOperationException, NotExistingEntityException {
		throw new MethodNotImplementedException("The onboarding of a new experiment descriptor is not allowed through the EEM");
	}
	
	@Override
	public void deleteExpDescriptor(String expDescriptorId, String tenantId)
			throws MethodNotImplementedException, MalformattedElementException, NotExistingEntityException, FailedOperationException {
		throw new MethodNotImplementedException("The deletion of an experiment descriptor is not allowed through the EEM");
	}

	@Override
	public void useExpDescriptor(String s, String s1) throws MethodNotImplementedException, MalformattedElementException, NotExistingEntityException, FailedOperationException {
		/*
		TODO: Complete the methods if needed
		 */
			throw new MethodNotImplementedException("The use of an experiment descriptor is not allowed through the EEM");
	}

	@Override
	public void releaseExpDescriptor(String s, String s1) throws MethodNotImplementedException, MalformattedElementException, NotExistingEntityException, FailedOperationException {
		/*
		TODO: Complete the methods if needed
		 */
		throw new MethodNotImplementedException("The release of an experiment descriptor is not allowed through the EEM");
	}

	@Override
	public Map<String, NfvNsInstantiationInfo> translateVsd(List<String> vsdIds)
			throws FailedOperationException, NotExistingEntityException, MethodNotImplementedException {
		throw new MethodNotImplementedException("The deletion of an experiment descriptor is not allowed through the EEM");
	}
	
	@PostConstruct
	private void initCatalogueRestClient() {
		log.debug("Initializing Experiment Catalogue REST client");
		String portalCatalogueBaseUrl = "http://" + catalogueHost;
		experimentCatalogueRestClient = new ExperimentCatalogueRestClient(portalCatalogueBaseUrl);
		log.debug("Experiment Catalogue REST client initialized with base URL: " + portalCatalogueBaseUrl);
	}

}
