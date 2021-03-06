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
package it.nextworks.eem.sbi.expcatalogue;

import it.nextworks.eem.sbi.RequestResponseLoggingInterceptor;
import it.nextworks.nfvmano.catalogue.blueprint.elements.*;
import it.nextworks.nfvmano.catalogue.blueprint.messages.*;
import it.nextworks.nfvmano.catalogue.translator.NfvNsInstantiationInfo;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ExperimentCatalogueRestClient {

	private static final Logger log = LoggerFactory.getLogger(ExperimentCatalogueRestClient.class);
	
	private RestTemplate restTemplate;
	
	private String catalogueUrl;
	
	public ExperimentCatalogueRestClient(String baseUrl) {

	    this.catalogueUrl = baseUrl + "/portal/catalogue";
	    this.restTemplate= new RestTemplate(new BufferingClientHttpRequestFactory(
				new SimpleClientHttpRequestFactory()
		));
	    this.restTemplate.setInterceptors(Collections.singletonList(new RequestResponseLoggingInterceptor()));

	}

	public QueryExpBlueprintResponse queryExperimentBlueprint(String blueprintId) throws FailedOperationException {
		log.debug("Building HTTP request for querying Experiment Blueprint with ID " + blueprintId);
		HttpHeaders header = new HttpHeaders();
		header.add("Content-Type", "application/json");
		
		HttpEntity<?> getEntity = new HttpEntity<>(header);
		
		String url = catalogueUrl + "/expblueprint/" + blueprintId;
		try {
			log.debug("Sending HTTP request to retrieve experiment blueprint.");
			
			ResponseEntity<ExpBlueprintInfo> httpResponse =
					restTemplate.exchange(url, HttpMethod.GET, getEntity, ExpBlueprintInfo.class);
			
			log.debug("Response code: " + httpResponse.getStatusCode().toString());
			HttpStatus code = httpResponse.getStatusCode();
			
			if (code.equals(HttpStatus.OK)) {
				log.debug("Experiment blueprint correctly retrieved");

				//Generated response based on the single expblueprintinfo returned by the catalogue to be more generic
                List<ExpBlueprintInfo> queryList = new ArrayList<>();
                queryList.add(httpResponse.getBody());

				return new QueryExpBlueprintResponse(queryList);
			}else
				return null;
		} catch (HttpClientErrorException e) {
			throw new FailedOperationException("Error during Experiment Blueprint retrieval : Client error");
		} catch (HttpServerErrorException e) {
			throw new FailedOperationException("Error during Experiment Blueprint retrieval : Server error");
		} catch (UnknownHttpStatusCodeException e) {
			throw new FailedOperationException("Error during Experiment Blueprint retrieval: Unknown error");
		} catch (Exception e) {
			throw new FailedOperationException("Generic error while interacting with Portal Catalogue at url " + url);
		}
	}
	
	public QueryExpDescriptorResponse queryExperimentDescriptor(String descriptorId) throws FailedOperationException {
		log.debug("Building HTTP request for querying Experiment Descriptor with ID " + descriptorId);
		HttpHeaders header = new HttpHeaders();
		header.add("Content-Type", "application/json");
		header.setAccept(new ArrayList<>());
		HttpEntity<?> getEntity = new HttpEntity<>(header);
		
		String url = catalogueUrl + "/expdescriptor/" + descriptorId;
		try {
			log.debug("Sending HTTP request to retrieve experiment descriptor.");
			
			ResponseEntity<ExpDescriptor> httpResponse =
					restTemplate.exchange(url, HttpMethod.GET, getEntity, ExpDescriptor.class);
			
			log.debug("Response code: " + httpResponse.getStatusCode().toString());
			HttpStatus code = httpResponse.getStatusCode();
			
			if (code.equals(HttpStatus.OK)) {
				//Modified this to match the API from the Catalogue, but maintained the query as return to avoid affecting the rest of the code
				log.debug("Experiment descriptor correctly retrieved");
				ExpDescriptor res = httpResponse.getBody();
				ArrayList<ExpDescriptor> resList = new ArrayList<>();
				resList.add(res);
				return new QueryExpDescriptorResponse(resList);
			}else
				return null;
		} catch (HttpClientErrorException e) {
			throw new FailedOperationException("Error during Experiment Descriptor retrieval : Client error");
		} catch (HttpServerErrorException e) {
			throw new FailedOperationException("Error during Experiment Descriptor retrieval : Server error");
		} catch (UnknownHttpStatusCodeException e) {
			throw new FailedOperationException("Error during Experiment Descriptor retrieval: Unknown error");
		} catch (Exception e) {
			throw new FailedOperationException("Generic error while interacting with Portal Catalogue at url " + url);
		}
	}
	
	public QueryVsBlueprintResponse queryVsBlueprint(String blueprintId) throws FailedOperationException {
		log.debug("Building HTTP request for querying VS blueprint with ID " + blueprintId);
		HttpHeaders header = new HttpHeaders();
		header.add("Content-Type", "application/json");
		
		HttpEntity<?> getEntity = new HttpEntity<>(header);
		
		String url = catalogueUrl + "/vsblueprint/" + blueprintId;
		try {
			log.debug("Sending HTTP request to retrieve VS blueprint.");
			
			ResponseEntity<VsBlueprintInfo> httpResponse =
					restTemplate.exchange(url, HttpMethod.GET, getEntity, VsBlueprintInfo.class);
			
			log.debug("Response code: " + httpResponse.getStatusCode().toString());
			HttpStatus code = httpResponse.getStatusCode();
			
			if (code.equals(HttpStatus.OK)) {
				log.debug("VS blueprint correctly retrieved");
				List<VsBlueprintInfo> queryList = new ArrayList<>();
				queryList.add(httpResponse.getBody());

				//Generated the response from the single instance returned by the catalogue
				return new QueryVsBlueprintResponse(queryList);
			}else
				return null;
		} catch (HttpClientErrorException e) {
			throw new FailedOperationException("Error during Vertical Service Blueprint retrieval : Client error");
		} catch (HttpServerErrorException e) {
			throw new FailedOperationException("Error during Vertical Service Blueprint retrieval : Server error");
		} catch (UnknownHttpStatusCodeException e) {
			throw new FailedOperationException("Error during Vertical Service Blueprint retrieval: Unknown error");
		} catch (Exception e) {
			throw new FailedOperationException("Generic error while interacting with Portal Catalogue at url " + url);
		}
	}

	public QueryVsDescriptorResponse queryVsDescriptor(String descriptorId) throws FailedOperationException {
		log.debug("Building HTTP request for querying VS Descriptor with ID " + descriptorId);
		HttpHeaders header = new HttpHeaders();
		header.add("Content-Type", "application/json");
		
		HttpEntity<?> getEntity = new HttpEntity<>(header);
		
		String url = catalogueUrl + "/vsdescriptor/" + descriptorId;
		try {
			log.debug("Sending HTTP request to retrieve VS descriptor.");
			
			ResponseEntity<VsDescriptor> httpResponse =
					restTemplate.exchange(url, HttpMethod.GET, getEntity, VsDescriptor.class);
			
			log.debug("Response code: " + httpResponse.getStatusCode().toString());
			HttpStatus code = httpResponse.getStatusCode();
			
			if (code.equals(HttpStatus.OK)) {
				log.debug("VS descriptor correctly retrieved");
				List<VsDescriptor> queryList = new ArrayList<>();
				queryList.add(httpResponse.getBody());
				return new QueryVsDescriptorResponse(queryList);
			}else
				return null;
		} catch (HttpClientErrorException e) {
			throw new FailedOperationException("Error during Vertical Service Descriptor retrieval : Client error");
		} catch (HttpServerErrorException e) {
			throw new FailedOperationException("Error during Vertical Service Descriptor retrieval : Server error");
		} catch (UnknownHttpStatusCodeException e) {
			throw new FailedOperationException("Error during Vertical Service Descriptor retrieval: Unknown error");
		} catch (Exception e) {
			throw new FailedOperationException("Generic error while interacting with Portal Catalogue at url " + url);
		}
	}
	
	public QueryCtxBlueprintResponse queryCtxBlueprint(String blueprintId) throws FailedOperationException {
		log.debug("Building HTTP request for querying CTX blueprint with ID " + blueprintId);
		HttpHeaders header = new HttpHeaders();
		header.add("Content-Type", "application/json");
		
		HttpEntity<?> getEntity = new HttpEntity<>(header);
		
		String url = catalogueUrl + "/ctxblueprint/" + blueprintId;
		try {
			log.debug("Sending HTTP request to retrieve CTX blueprint.");
			
			ResponseEntity<CtxBlueprintInfo> httpResponse =
					restTemplate.exchange(url, HttpMethod.GET, getEntity, CtxBlueprintInfo.class);
			
			log.debug("Response code: " + httpResponse.getStatusCode().toString());
			HttpStatus code = httpResponse.getStatusCode();
			
			if (code.equals(HttpStatus.OK)) {
				log.debug("CTX blueprint correctly retrieved");
				List<CtxBlueprintInfo> queryList = new ArrayList<>();
				queryList.add(httpResponse.getBody());
				return new QueryCtxBlueprintResponse(queryList);
			}else
				return null;
		} catch (HttpClientErrorException e) {
			throw new FailedOperationException("Error during Context Blueprint retrieval : Client error");
		} catch (HttpServerErrorException e) {
			throw new FailedOperationException("Error during Context Blueprint retrieval : Server error");
		} catch (UnknownHttpStatusCodeException e) {
			throw new FailedOperationException("Error during Context Blueprint retrieval: Unknown error");
		} catch (Exception e) {
			throw new FailedOperationException("Generic error while interacting with Portal Catalogue at url " + url);
		}
	}
	
	public QueryCtxDescriptorResponse queryCtxDescriptor(String descriptorId) throws FailedOperationException {
		log.debug("Building HTTP request for querying CTX Descriptor with ID " + descriptorId);
		HttpHeaders header = new HttpHeaders();
		header.add("Content-Type", "application/json");
		
		HttpEntity<?> getEntity = new HttpEntity<>(header);
		
		String url = catalogueUrl + "/ctxdescriptor/" + descriptorId;
		try {
			log.debug("Sending HTTP request to retrieve VS descriptor.");
			
			ResponseEntity<CtxDescriptor> httpResponse =
					restTemplate.exchange(url, HttpMethod.GET, getEntity, CtxDescriptor.class);
			
			log.debug("Response code: " + httpResponse.getStatusCode().toString());
			HttpStatus code = httpResponse.getStatusCode();
			
			if (code.equals(HttpStatus.OK)) {
				log.debug("CTX descriptor correctly retrieved");
				List<CtxDescriptor> queryList = new ArrayList<>();
				queryList.add(httpResponse.getBody());
				return new QueryCtxDescriptorResponse(queryList);
			}else
				return null;
		} catch (HttpClientErrorException e) {
			throw new FailedOperationException("Error during Context Descriptor retrieval : Client error");
		} catch (HttpServerErrorException e) {
			throw new FailedOperationException("Error during Context Descriptor retrieval : Server error");
		} catch (UnknownHttpStatusCodeException e) {
			throw new FailedOperationException("Error during Context Descriptor retrieval: Unknown error");
		} catch (Exception e) {
			throw new FailedOperationException("Generic error while interacting with Portal Catalogue at url " + url);
		}
	}
	
	public QueryTestCaseBlueprintResponse queryTestCaseBlueprint(String blueprintId) throws FailedOperationException {
		log.debug("Building HTTP request for querying TC blueprint with ID " + blueprintId);
		HttpHeaders header = new HttpHeaders();
		header.add("Content-Type", "application/json");
		
		HttpEntity<?> getEntity = new HttpEntity<>(header);
		
		String url = catalogueUrl + "/testcaseblueprint/" + blueprintId;
		try {
			log.debug("Sending HTTP request to retrieve TC blueprint.");
			
			ResponseEntity<TestCaseBlueprintInfo> httpResponse =
					restTemplate.exchange(url, HttpMethod.GET, getEntity, TestCaseBlueprintInfo.class);
			
			log.debug("Response code: " + httpResponse.getStatusCode().toString());
			HttpStatus code = httpResponse.getStatusCode();
			
			if (code.equals(HttpStatus.OK)) {
				log.debug("TC blueprint correctly retrieved");
				List<TestCaseBlueprintInfo> queryList = new ArrayList<>();
				queryList.add(httpResponse.getBody());
				return new QueryTestCaseBlueprintResponse(queryList);
			}else
				return null;
		} catch (HttpClientErrorException e) {
			throw new FailedOperationException("Error during Test Case Blueprint retrieval : Client error");
		} catch (HttpServerErrorException e) {
			throw new FailedOperationException("Error during Test Case Blueprint retrieval : Server error");
		} catch (UnknownHttpStatusCodeException e) {
			throw new FailedOperationException("Error during Test Case Blueprint retrieval: Unknown error");
		} catch (Exception e) {
			throw new FailedOperationException("Generic error while interacting with Portal Catalogue at url " + url);
		}
	}
	
	public QueryTestCaseDescriptorResponse queryTestCaseDescriptor(String descriptorId) throws FailedOperationException {
		log.debug("Building HTTP request for querying TC Descriptor with ID " + descriptorId);
		HttpHeaders header = new HttpHeaders();
		header.add("Content-Type", "application/json");
		
		HttpEntity<?> getEntity = new HttpEntity<>(header);
		
		String url = catalogueUrl + "/testcasedescriptor/" + descriptorId;
		try {
			log.debug("Sending HTTP request to retrieve VS descriptor.");
			
			ResponseEntity<TestCaseDescriptor> httpResponse =
					restTemplate.exchange(url, HttpMethod.GET, getEntity, TestCaseDescriptor.class);
			
			log.debug("Response code: " + httpResponse.getStatusCode().toString());
			HttpStatus code = httpResponse.getStatusCode();
			
			if (code.equals(HttpStatus.OK)) {
				log.debug("TC descriptor correctly retrieved");
				List<TestCaseDescriptor> queryList = new ArrayList<>();
				queryList.add(httpResponse.getBody());
				return new QueryTestCaseDescriptorResponse(queryList);
			}else
				return null;
		} catch (HttpClientErrorException e) {
			throw new FailedOperationException("Error during Test Case Descriptor retrieval : Client error");
		} catch (HttpServerErrorException e) {
			throw new FailedOperationException("Error during Test Case Descriptor retrieval : Server error");
		} catch (UnknownHttpStatusCodeException e) {
			throw new FailedOperationException("Error during Test Case Descriptor retrieval: Unknown error");
		} catch (Exception e) {
			throw new FailedOperationException("Generic error while interacting with Portal Catalogue at url " + url);
		}
	}
	
	public NfvNsInstantiationInfo translateExpd(String expdId) throws FailedOperationException {
		log.debug("Building HTTP request for translation of Experiment Descriptor with ID " + expdId);
		HttpHeaders header = new HttpHeaders();
		header.add("Content-Type", "application/json");
		
		HttpEntity<?> getEntity = new HttpEntity<>(header);
		
		String url = catalogueUrl + "/translator/expd/" + expdId;
		try {
			log.debug("Sending HTTP request to retrieve experiment descriptor translation into NFV NS.");
			
			ResponseEntity<NfvNsInstantiationInfo> httpResponse =
					restTemplate.exchange(url, HttpMethod.GET, getEntity, NfvNsInstantiationInfo.class);
			
			log.debug("Response code: " + httpResponse.getStatusCode().toString());
			HttpStatus code = httpResponse.getStatusCode();
			
			if (code.equals(HttpStatus.OK)) {
				log.debug("Experiment descriptor translation correctly retrieved");
				return httpResponse.getBody();
			}else
				return null;
		} catch (HttpClientErrorException e) {
			throw new FailedOperationException("Error during Translated Experiment Descriptor retrieval : Client error");
		} catch (HttpServerErrorException e) {
			throw new FailedOperationException("Error during Translated Experiment Descriptor : Server error");
		} catch (UnknownHttpStatusCodeException e) {
			throw new FailedOperationException("Error during Translated Experiment Descriptor Unknown error");
		} catch (Exception e) {
			throw new FailedOperationException("Generic error while interacting with Portal Catalogue at url " + url);
		}
	}
}
