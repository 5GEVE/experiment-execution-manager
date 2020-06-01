package it.nextworks.eem.sbi.runtimeConfigurator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;

import it.nextworks.eem.model.ConfigurationStatus;
import it.nextworks.eem.model.MetricInfo;
import it.nextworks.eem.rabbitMessage.AbortingResultInternalMessage;
import it.nextworks.eem.rabbitMessage.ConfigurationResultInternalMessage;
import it.nextworks.eem.rabbitMessage.InternalMessage;
import it.nextworks.eem.rabbitMessage.TestCaseResultInternalMessage;
import it.nextworks.eem.sbi.interfaces.ConfiguratorServiceProviderInterface;
import it.nextworks.eem.sbi.interfaces.ExecutorServiceProviderInterface;
import it.nextworks.eem.sbi.runtimeConfigurator.client.api.RcnbiControllerApi;
import it.nextworks.eem.sbi.runtimeConfigurator.client.ApiClient;
import it.nextworks.eem.sbi.runtimeConfigurator.client.ApiException;
import it.nextworks.eem.sbi.runtimeConfigurator.client.model.*;

import it.nextworks.nfvmano.catalogue.blueprint.elements.EveSite;
import it.nextworks.nfvmano.catalogue.blueprint.elements.InfrastructureMetric;

public class RCDriver implements ConfiguratorServiceProviderInterface, ExecutorServiceProviderInterface {

	// static variable single_instance of type RCDriver
	private static RCDriver single_instance = null;

	private static final Logger log = LoggerFactory.getLogger(RCDriver.class);

	private RabbitTemplate rabbitTemplate;
	private TopicExchange messageExchange;

	private RcnbiControllerApi rcApi;
	
	// experiments correlates executionId as provided by ELM with configId/metricId/execId as received by RC
	Map<String, String> experiments = new HashMap<>();

	// private constructor restricted to this class itself
	private RCDriver(String rcURI, RabbitTemplate rabbitTemplate, TopicExchange messageExchange) throws URISyntaxException {
		log.debug("Initializing RC Driver : uri {}", rcURI);
		
		ApiClient apiClient = new ApiClient();
		apiClient.setBasePath (rcURI);
		rcApi = new RcnbiControllerApi (apiClient);
		
		this.rabbitTemplate = rabbitTemplate;
		this.messageExchange = messageExchange;
	}

	// static method to create instance of RCDriver class
	public static RCDriver getInstance(String rcURI, RabbitTemplate rabbitTemplate, TopicExchange messageExchange) throws URISyntaxException {
		if (single_instance == null)
			single_instance = new RCDriver(rcURI, rabbitTemplate, messageExchange);
		else
			log.debug("RC Driver already instantiated: uri {}", rcURI);
		return single_instance;
	}

	@Override
	public void applyConfiguration(String executionId, String tcDescriptorId, String configScript, String resetScript){
		new Thread(() -> {applyConfigurationImplementation(executionId, tcDescriptorId, configScript, resetScript);}).start();
	}

	@Override
	public void resetConfiguration(String executionId, String tcDescriptorId, String configId){
		new Thread(() -> {resetConfigurationImplementation(executionId, tcDescriptorId, configId);}).start();
	}
	
	@Override
	public void abortConfiguration(String executionId, String tcDescriptorId, String configId){
		new Thread(() -> {abortConfigurationImplementation(executionId, tcDescriptorId, configId);}).start();
	}

	@Override
	public void configureInfrastructureMetricCollection(String executionId, String tcDescriptorId, List<MetricInfo> metrics){
		new Thread(() -> {configureInfrastructureMetricCollectionImplementation(executionId, tcDescriptorId, metrics);}).start();
	}

	@Override
	public void removeInfrastructureMetricCollection(String executionId, String tcDescriptorId, String metricConfigId){
		new Thread(() -> {removeInfrastructureMetricCollectionImplementation(executionId, tcDescriptorId, metricConfigId);}).start();
	}

	@Override
	public void runTestCase(String executionId, String tcDescriptorId, String testCaseFile){
		new Thread(() -> {runTestCaseImplementation(executionId, tcDescriptorId, testCaseFile);}).start();
	}

	@Override
	public void abortTestCase(String executionId, String tcDescriptorId){
		new Thread(() -> {abortTestCaseImplementation(executionId, tcDescriptorId);}).start();
	}

	private void applyConfigurationImplementation (String executionId, String tcDescriptorId, String configScript, String resetScript) {

		// PROCESS: Day-2 Configuration INIT
		log.debug("PROCESS: Day-2 Configuration INIT. Initializing day-2 configuration task for Test Case {} with executionId {}", tcDescriptorId, executionId);
		String result = "";
		String configId = "";
		String experiments_key = "config_" + tcDescriptorId + "_" + executionId;
								
		// PROCESS: Request configId from RC
		log.debug("PROCESS: Request configId from RC. Requesting configId for Test Case {} with executionId {}", tcDescriptorId, executionId);
		ApplicationDay2ConfigurationWrapper day2Wrapper = new ApplicationDay2ConfigurationWrapper();
		day2Wrapper.setConfigurationScript (configScript);
		day2Wrapper.setResetConfigScript (resetScript);

		try {
			ApplicationDay2ConfigurationResponse configIdResponse = rcApi.applicationDay2ConfigurationInit(day2Wrapper);
			configId = configIdResponse.getConfigurationId();
			log.debug("PROCESS: Request configId from RC. configId for Test Case {} with executionId {} is: {}", tcDescriptorId, executionId, configId);

			// we associate the configId with the specific ELM-provided ID; this is needed for the reset method
			experiments.put(experiments_key, configId);

		} catch (ApiException e1) {
			log.error("PROCESS: Request configId from RC. ApiException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e1.getMessage());
			manageConfigurationError("API Failure while requesting configId for application day-2 configuration", executionId);
			return;
		}

		// PROCESS: Start day-2 configuration
		log.debug("PROCESS: Start day-2 configuration. Starting configuration process for Test Case {} with executionId {}", tcDescriptorId, executionId);
		boolean isConfiguring = true;
		
		try {
			ApplicationDay2ConfigurationStatusResponse startResponse = rcApi.applicationDay2ConfigurationStart(configId);
			
			switch (startResponse.getStatus()) {
			case COMPLETED: result = "OK"; isConfiguring = false;
			case ABORTED: result = "ABORTED"; isConfiguring = false;
			case STOPPED: result = "STOPPED"; isConfiguring = false;
			case FAILED: result = "FAILED"; isConfiguring = false;
			default:;
			}
			
		} catch (ApiException e2) {
			log.error("PROCESS: Start day-2 configuration. ApiException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e2.getMessage());
			manageConfigurationError("API Failure while starting application day-2 configuration", executionId);
			return;
		}		

		// PROCESS: Day-2 configuration loop
		try {
			while (isConfiguring) {
				ApplicationDay2ConfigurationStatusResponse statusResponse = rcApi.applicationDay2ConfigurationStatus(configId);
				
				switch (statusResponse.getStatus()) {
				case COMPLETED: result = "OK"; isConfiguring = false;
				case ABORTED: result = "ABORTED"; isConfiguring = false;
				case STOPPED: result = "STOPPED"; isConfiguring = false;
				case FAILED: result = "FAILED"; isConfiguring = false;
				default: Thread.sleep(3000);
				}
			}
		} catch (ApiException e3) {
			log.error("PROCESS: Day-2 configuration loop. ApiException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e3.getMessage());
			manageConfigurationError("API Failure while waiting for application day-2 configuration", executionId);
			return;
		} catch (InterruptedException e4) {
			log.error("PROCESS: Day-2 configuration loop. InterruptedException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e4.getMessage());
			manageConfigurationError("Interruption while waiting for application day-2 configuration", executionId);
			return;
		}

		log.debug("PROCESS: Day-2 configuration loop. Configuration process for Test Case {} with executionId {} finished with result {}", tcDescriptorId, executionId, result);
		
		// Evaluation of results for configuration job
		switch (result) {
		case "OK": manageConfigurationOK(result, executionId, tcDescriptorId); break;
		case "ABORTED": manageConfigurationError("Day-2 Configuration task was ABORTED", executionId); break;
		case "STOPPED": manageConfigurationError("Day-2 Configuration task was STOPPED", executionId); break;
		case "FAILED": manageConfigurationError("Day-2 Configuration task FAILED", executionId); break;
		default: manageConfigurationError("Status for Day-2 Configuration is UNKNOWN", executionId); break;
		}
	}

	//TODO: does EEM really provide configId? how does it know it? I'm not providing it back in applyConfigurationImplementation...
	private void resetConfigurationImplementation(String executionId, String tcDescriptorId, String notSoClearId){
		
		// PROCESS: Day-2 Configuration reset INIT
		log.debug("PROCESS: Day-2 Configuration RESET. Initializing day-2 configuration reset for Test Case {} with executionId {}", tcDescriptorId, executionId);
		String experiments_key = "config_" + tcDescriptorId + "_" + executionId;
		String configId = experiments.get(experiments_key); // we retrieve the configId from experiments
		String result = "";

		// PROCESS: Reset day-2 configuration
		log.debug("Reset day-2 configuration. Starting reset process for Test Case {} with executionId {}", tcDescriptorId, executionId);
		boolean isReseting = true;
				
		try {
			ApplicationDay2ConfigurationStatusResponse resetResponse = rcApi.applicationDay2ConfigurationReset(configId);
			
			switch (resetResponse.getStatus()) {
			case CLEANED: result = "CLEANED"; isReseting = false;
			case ABORTED: result = "ABORTED"; isReseting = false;
			case STOPPED: result = "STOPPED"; isReseting = false;
			case FAILED: result = "FAILED"; isReseting = false;
			default:;
			}
		} catch (ApiException e1) {
			log.error("PROCESS: Start day-2 configuration. ApiException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e1.getMessage());
			manageConfigurationError("API Failure while starting application day-2 configuration", executionId);
			return;
		}	
		
		// PROCESS: Reset day-2 configuration loop
		try {
			while (isReseting) {
				ApplicationDay2ConfigurationStatusResponse statusResponse = rcApi.applicationDay2ConfigurationStatus(configId);
				
				switch (statusResponse.getStatus()) {
				case CLEANED: result = "CLEANED"; isReseting = false;
				case ABORTED: result = "ABORTED"; isReseting = false;
				case STOPPED: result = "STOPPED"; isReseting = false;
				case FAILED: result = "FAILED"; isReseting = false;
				default: Thread.sleep(3000);
				}
			}
		} catch (ApiException e2) {
			log.error("PROCESS: Reset day-2 configuration. ApiException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e2.getMessage());
			manageConfigurationError("API Failure while reseting application day-2 configuration", executionId);
			return;
		} catch (InterruptedException e3) {
			log.error("PROCESS: Reset day-2 configuration. InterruptedException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e3.getMessage());
			manageConfigurationError("Interruption while reseting application day-2 configuration", executionId);
			return;
		}
		
		log.debug("PROCESS: Reset day-2 configuration loop. Configuration reset for Test Case {} with executionId {} finished with result {}", tcDescriptorId, executionId, result);
	
		// we remove the configuration job from the list of jobs
		experiments.remove(experiments_key, configId);


		// Evaluation of results for configuration reset job
		switch (result) {
		case "OK": {
			String topic = "lifecycle.configurationResult." + executionId;
			InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.CONF_RESET, result, null,false);
			try {
				sendMessageToQueue(internalMessage, topic);
			} catch (JsonProcessingException e) {
				log.error("Error while translating internal scheduling message in JSON format");
				manageConfigurationError("Error while translating internal scheduling message in Json format", executionId);
			}
		}
		case "ABORTED": manageConfigurationError("Day-2 Configuration reset task was ABORTED", executionId); break;
		case "STOPPED": manageConfigurationError("Day-2 Configuration reset task was STOPPED", executionId); break;
		case "FAILED": manageConfigurationError("Day-2 Configuration reset task FAILED", executionId); break;
		default: manageConfigurationError("Status for Day-2 Configuration reset is UNKNOWN", executionId); break;
		}

	}

	private void abortConfigurationImplementation(String executionId, String tcDescriptorId, String configId){
		//TODO abort configuration
		//no response message needed
	}

	private void configureInfrastructureMetricCollectionImplementation(String executionId, String tcDescriptorId, List<MetricInfo> metrics){

		// PROCESS: Infrastructure Metrics Configuration INIT
		log.debug("PROCESS: Infrastructure Metrics Configuration INIT. Initializing metrics configuration task for Test Case {} with executionId {}", tcDescriptorId, executionId);
		String result = "";
		String metricsConfigId = "";
		// TODO: apparently this will not be needed since we get the metricsConfigId in the removal --> confirm and remove
		String experiments_key = "metricsConfig_" + tcDescriptorId + "_" + executionId;

		// PROCESS: Request metricsConfigId from RC
		log.debug("PROCESS: Request metricsConfigId from RC. Requesting metricsConfigId for Test Case {} with executionId {}", tcDescriptorId, executionId);

		int numberOfMetrics = metrics.size();
		InfrastructureDay2ConfigurationWrapper metricsWrapper = new InfrastructureDay2ConfigurationWrapper();
		List<InfrastructureMetricWrapper> metricsList = new ArrayList<>();

		for (int i = 1; i <= numberOfMetrics; i++) {
			InfrastructureMetricWrapper metricWrapper = new InfrastructureMetricWrapper();

			//TODO: check how to ask for these parameters in classes EveSite and InfrastructureMetric, from MetricInfo
			String metricId = metrics.getInfrastructureMetric(i).getMetricId();
			metricWrapper.setMetricId(metricId);
			String unit = metrics.getInfrastructureMetric(i).getUnit();
			metricWrapper.setUnit(unit);
			String interval = metrics.getInfrastructureMetric(i).getInterval();
			metricWrapper.setInterval(interval);
			String topic = metrics.getTopic(i);
			metricWrapper.setTopic(topic);
			String site = metrics.getEveSite(i).getSiteName();
			metricWrapper.setSite(site);

			metricsList.add(metricWrapper);
		}

		metricsWrapper.setInfrastructureMetricsInfo(metricsList);

		try {
			InfrastructureDay2ConfigurationResponse metricsConfigIdResponse = rcApi.infrastructureDay2ConfigurationInit(metricsWrapper);
			metricsConfigId = metricsConfigIdResponse.getConfigurationId();
			log.debug("PROCESS: Request metricsConfigId from RC. metricsConfigId for Test Case {} with executionId {} is: {}", tcDescriptorId, executionId, metricsConfigId);

			// TODO: we associate the metricsConfigId with the specific ELM-provided ID --> confirm this is not needed for the reset method and remove
			experiments.put(experiments_key, metricsConfigId);

		} catch (ApiException e1) {
			log.error("PROCESS: Request metricsConfigId from RC. ApiException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e1.getMessage());
			manageConfigurationError("API Failure while requesting metricsConfigId for infrastructure metrics configuration", executionId);
			return;
		}

		// PROCESS: Start metrics configuration
		log.debug("PROCESS: Start metrics configuration. Starting metrics configuration process for Test Case {} with executionId {}", tcDescriptorId, executionId);
		boolean isConfiguring = true;

		try {
			InfrastructureDay2ConfigurationStatusResponse startResponse = rcApi.infrastructureDay2ConfigurationStart(metricsConfigId);

			switch (startResponse.getStatus()) {
			case COMPLETED: result = "OK"; isConfiguring = false;
			case ABORTED: result = "ABORTED"; isConfiguring = false;
			case STOPPED: result = "STOPPED"; isConfiguring = false;
			case FAILED: result = "FAILED"; isConfiguring = false;
			default:;
			}

		} catch (ApiException e2) {
			log.error("PROCESS: Start metrics configuration. ApiException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e2.getMessage());
			manageConfigurationError("API Failure while starting infrastructure metrics configuration", executionId);
			return;
		}		

		// PROCESS: Metrics configuration loop
		try {
			while (isConfiguring) {
				InfrastructureDay2ConfigurationStatusResponse statusResponse = rcApi.infrastructureDay2ConfigurationStatus(metricsConfigId);

				switch (statusResponse.getStatus()) {
				case COMPLETED: result = "OK"; isConfiguring = false;
				case ABORTED: result = "ABORTED"; isConfiguring = false;
				case STOPPED: result = "STOPPED"; isConfiguring = false;
				case FAILED: result = "FAILED"; isConfiguring = false;
				default: Thread.sleep(3000);
				}
			}
		} catch (ApiException e3) {
			log.error("PROCESS: Metrics configuration loop. ApiException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e3.getMessage());
			manageConfigurationError("API Failure while waiting for infrastructure metrics configuration", executionId);
			return;
		} catch (InterruptedException e4) {
			log.error("PROCESS: Metrics configuration loop. InterruptedException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e4.getMessage());
			manageConfigurationError("Interruption while waiting for infrastructure metrics configuration", executionId);
			return;
		}

		log.debug("PROCESS: Metrics configuration loop. Metrics configuration process for Test Case {} with executionId {} finished with result {}", tcDescriptorId, executionId, result);

		// Evaluation of results for metrics configuration job
		switch (result) {
		case "OK": {
			// so far there is a single metricsConfigId that relates to all metrics. TODO: evaluate if this needs change
			List<String> metricConfigIds = new ArrayList<>();
			metricConfigIds.add("metricsConfigId");
			String topic = "lifecycle.configurationResult." + executionId;
			InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.METRIC_CONFIGURED, result, metricConfigIds, false);
			try {
				sendMessageToQueue(internalMessage, topic);
			} catch (JsonProcessingException e) {
				log.error("Error while translating internal scheduling message in JSON format");
				manageConfigurationError("Error while translating internal scheduling message in Json format", executionId);
			}
			
			//TODO: Proposal by Leonardo
//			String topic = "lifecycle.configurationResult." + executionId;
//	        InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.METRIC_CONFIGURED, result, "metricConfigId",false);  //Not a list anymore, I expect the metricConfigId (the one between RCDriver and RC) in the response message
//	        try {
//	            sendMessageToQueue(internalMessage, topic);
//	        } catch (JsonProcessingException e) {
//	            log.error("Error while translating internal scheduling message in Json format");
//	            manageConfigurationError("Error while translating internal scheduling message in Json format", executionId);
//	        }
			
		}
		case "ABORTED": manageConfigurationError("Metrics Configuration task was ABORTED", executionId); break;
		case "STOPPED": manageConfigurationError("Metrics Configuration task was STOPPED", executionId); break;
		case "FAILED": manageConfigurationError("Metrics Configuration task FAILED", executionId); break;
		default: manageConfigurationError("Status for Metrics Configuration is UNKNOWN", executionId); break;
		}

	}

	// TODO: in this method, metricsConfigId is fine since it is returned by the config method. Still, it refers to all metrics at the same time (today)
	private void removeInfrastructureMetricCollectionImplementation(String executionId, String tcDescriptorId, String metricsConfigId){

		// PROCESS: Metrics stop INIT
		log.debug("PROCESS: Metrics stop INIT. Initializing infrastructure metrics stop for Test Case {} with executionId {}", tcDescriptorId, executionId);
		String result = "";

		// PROCESS: Stop metrics
		log.debug("PROCESS: Stop metrics. Stopping infrastructure metrics for Test Case {} with executionId {}", tcDescriptorId, executionId);
		boolean isReseting = true;

		try {
			InfrastructureDay2ConfigurationStatusResponse stopResponse = rcApi.infrastructureDay2ConfigurationStop(metricsConfigId);

			switch (stopResponse.getStatus()) {
			case CLEANED: result = "CLEANED"; isReseting = false;
			case ABORTED: result = "ABORTED"; isReseting = false;
			case STOPPED: result = "STOPPED"; isReseting = false;
			case FAILED: result = "FAILED"; isReseting = false;
			default:;
			}
		} catch (ApiException e1) {
			log.error("PROCESS: Stop metrics. ApiException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e1.getMessage());
			manageConfigurationError("API Failure while requesting to stop infrastructure metrics", executionId);
			return;
		}	

		// PROCESS: Stop metrics loop
		try {
			while (isReseting) {
				InfrastructureDay2ConfigurationStatusResponse statusResponse = rcApi.infrastructureDay2ConfigurationStatus(metricsConfigId);

				switch (statusResponse.getStatus()) {
				case CLEANED: result = "CLEANED"; isReseting = false;
				case ABORTED: result = "ABORTED"; isReseting = false;
				case STOPPED: result = "STOPPED"; isReseting = false;
				case FAILED: result = "FAILED"; isReseting = false;
				default: Thread.sleep(3000);
				}
			}
		} catch (ApiException e2) {
			log.error("PROCESS: Stop metrics loop. ApiException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e2.getMessage());
			manageConfigurationError("API Failure while waiting to stop infrastructure metrics", executionId);
			return;
		} catch (InterruptedException e3) {
			log.error("PROCESS: Stop metrics loop. InterruptedException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e3.getMessage());
			manageConfigurationError("Interruption while waiting to stop infrastructure metrics", executionId);
			return;
		}

		log.debug("PROCESS: Stop metrics. Metrics for Test Case {} with executionId {} stopped with result {}", tcDescriptorId, executionId, result);

		// Evaluation of results for configuration reset job
		switch (result) {
		case "OK": {
			String topic = "lifecycle.configurationResult." + executionId;
			InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.METRIC_RESET, result, null,false);
			try {
				sendMessageToQueue(internalMessage, topic);
			} catch (JsonProcessingException e) {
				log.error("Error while translating internal scheduling message in Json format");
				manageConfigurationError("Error while translating internal scheduling message in JSON format", executionId);
			}
		}
		case "ABORTED": manageConfigurationError("Metrics stop task was ABORTED", executionId); break;
		case "STOPPED": manageConfigurationError("Metrics stop task was STOPPED", executionId); break;
		case "FAILED": manageConfigurationError("Metrics stop task FAILED", executionId); break;
		default: manageConfigurationError("Status for metrics stop task is UNKNOWN", executionId); break;
		}
	}

	private void runTestCaseImplementation(String executionId, String tcDescriptorId, String execScript){
		
		// PROCESS: Experiment Execution INIT
		log.debug("PROCESS: Experiment Execution INIT. Initializing experiment execution task for Test Case {} with executionId {}", tcDescriptorId, executionId);
		String result = "";
		String execId = "";
		
		// PROCESS: Request execId from RC
		log.debug("PROCESS: Request execId from RC. Requesting execId for Test Case {} with executionId {}", tcDescriptorId, executionId);
		ExecutionWrapper execWrapper = new ExecutionWrapper();
		execWrapper.setExecutionScript (execScript);
		
		try {
			ExecutionResponse execIdResponse = rcApi.executionInit(execWrapper);
			execId = execIdResponse.getExecutionId();
			log.debug("PROCESS: Request execId from RC. execId for Test Case {} with executionId {} is: {}", tcDescriptorId, executionId, execId);
		} catch (ApiException e1) {
			log.error("PROCESS: Request execId from RC. ApiException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e1.getMessage());
			manageTestCaseError("API Failure while requesting execId for experiment execution", executionId, tcDescriptorId);
			return;
		}

		// PROCESS: Start experiment execution
		log.debug("PROCESS: Start experiment execution. Starting execution process for Test Case {} with executionId {}", tcDescriptorId, executionId);
		boolean isRunning = true;

		try {
			ExecutionStatusResponse startResponse = rcApi.executionStart(execId);

			switch (startResponse.getStatus()) {
			case COMPLETED: result = "OK"; isRunning = false;
			case ABORTED: result = "ABORTED"; isRunning = false;
			case FAILED: result = "FAILED"; isRunning = false;
			default:;
			}

		} catch (ApiException e2) {
			log.error("PROCESS: Start experiment execution. ApiException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e2.getMessage());
			manageTestCaseError("API Failure while starting application day-2 configuration", executionId, tcDescriptorId);
			return;
		}		

		// PROCESS: Experiment execution loop
		try {
			while (isRunning) {
				ExecutionStatusResponse statusResponse = rcApi.executionStatus(execId);

				switch (statusResponse.getStatus()) {
				case COMPLETED: result = "OK"; isRunning = false;
				case ABORTED: result = "ABORTED"; isRunning = false;
				case FAILED: result = "FAILED"; isRunning = false;
				default: Thread.sleep(3000);
				}
			}
		} catch (ApiException e3) {
			log.error("PROCESS: Experiment execution loop. ApiException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e3.getMessage());
			manageTestCaseError("API Failure while waiting for experiment execution", executionId, tcDescriptorId);
			return;
		} catch (InterruptedException e4) {
			log.error("PROCESS: Experiment execution loop. InterruptedException for Test Case {} with executionId {}. Error {}", tcDescriptorId, executionId, e4.getMessage());
			manageTestCaseError("Interruption while waiting for experiment execution", executionId, tcDescriptorId);
			return;
		}

		log.debug("PROCESS: Experiment execution loop. Experiment execution process for Test Case {} with executionId {} finished with result {}", tcDescriptorId, executionId, result);

		// Evaluation of results for experiment execution job
		switch (result) {
		case "OK": manageExecutionOK(result, executionId, tcDescriptorId); break;
		case "ABORTED": manageTestCaseError("Experiment execution task was ABORTED", executionId, tcDescriptorId); break;
		case "FAILED": manageTestCaseError("Experiment execution task FAILED", executionId, tcDescriptorId); break;
		default: manageTestCaseError("Status for experiment execution is UNKNOWN", executionId, tcDescriptorId); break;
		}
		
	}

	private void abortTestCaseImplementation(String executionId, String tcDescriptorId){
		//TODO remove
		try {
			log.debug("Aborting the experiment");
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			log.debug("Sleep error");
		}

		//TODO abort test case
		//aborting ok
		String result = "OK";
		String topic = "lifecycle.abortingResult." + executionId;
		InternalMessage internalMessage = new AbortingResultInternalMessage(result, false);
		try {
			sendMessageToQueue(internalMessage, topic);
		} catch (JsonProcessingException e) {
			log.error("Error while translating internal scheduling message in Json format");
			manageAbortingError("Error while translating internal scheduling message in Json format", executionId);
		}

		//TODO handle aborting error
		//aborting ko
		//manageAbortingError();
	}

	//TODO: do not touch
	private void manageConfigurationOK (String result, String executionId, String tcDescriptorId) {
		String topic = "lifecycle.configurationResult." + executionId;
		InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.CONFIGURED, result, null, false);
		try {
			sendMessageToQueue(internalMessage, topic);
		} catch (JsonProcessingException e) {
			log.error("Error while translating internal scheduling message in JSON format");
			manageConfigurationError("Error while translating internal scheduling message in Json format", executionId);
		}
		
		//TODO: Proposal by Leonardo
//		String topic = "lifecycle.configurationResult." + executionId;
//        InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.CONFIGURED, result, "configId", false); //here the only difference is that I expect the configId (the one between RCDriver and RC) in the response message
//        try {
//            sendMessageToQueue(internalMessage, topic);
//        } catch (JsonProcessingException e) {
//            log.error("Error while translating internal scheduling message in Json format");
//            manageConfigurationError("Error while translating internal scheduling message in Json format", executionId);
//        }
	}

	//TODO: do not touch
	private void manageExecutionOK (String result, String executionId, String tcDescriptorId) {
		String topic = "lifecycle.executionResult." + executionId;
		InternalMessage internalMessage = new TestCaseResultInternalMessage(result, tcDescriptorId, false);
		try {
			sendMessageToQueue(internalMessage, topic);
		} catch (JsonProcessingException e) {
			log.error("Error while translating internal scheduling message in Json format");
			manageConfigurationError("Error while translating internal scheduling message in Json format", executionId);
		}
	}

	//TODO: do not touch
	private void manageConfigurationError(String errorMessage, String executionId){
		log.error("Configuration of Experiment Execution with Id {} failed: {}", executionId, errorMessage);
		errorMessage = String.format("Configuration of Experiment Execution with Id %s failed: %s", executionId, errorMessage);
		String topic = "lifecycle.configurationResult." + executionId;
		InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.FAILED, errorMessage, null, true);
		try {
			sendMessageToQueue(internalMessage, topic);
		} catch (JsonProcessingException e) {
			log.error("Error while translating internal scheduling message in JSON format");
			log.debug(null, e);
		}
	}

	//TODO: do not touch
	private void manageTestCaseError(String errorMessage, String executionId, String tcDescriptorId){
		log.error("Test Case with Id {} of Experiment Execution with Id {} failed: {}", tcDescriptorId, executionId, errorMessage);
		errorMessage = String.format("Test Case with Id %s for Experiment Execution with Id %s failed: %s", tcDescriptorId, executionId, errorMessage);
		String topic = "lifecycle.testCaseResult." + executionId;
		InternalMessage internalMessage = new TestCaseResultInternalMessage(errorMessage, tcDescriptorId, true);
		try {
			sendMessageToQueue(internalMessage, topic);
		} catch (JsonProcessingException e) {
			log.error("Error while translating internal scheduling message in Json format");
			log.debug(null, e);
		}
	}

	private void manageAbortingError(String errorMessage, String executionId){
		log.error("Aborting of Experiment Execution with Id {} failed : {}", executionId, errorMessage);
		errorMessage = String.format("Aborting of Experiment Execution with Id %s failed : %s", executionId, errorMessage);
		String topic = "lifecycle.abortingResult." + executionId;
		InternalMessage internalMessage = new AbortingResultInternalMessage(errorMessage, true);
		try {
			sendMessageToQueue(internalMessage, topic);
		} catch (JsonProcessingException e) {
			log.error("Error while translating internal scheduling message in Json format");
			log.debug(null, e);
		}
	}

	private void sendMessageToQueue(InternalMessage msg, String topic) throws JsonProcessingException {
		ObjectMapper mapper = buildObjectMapper();
		String json = mapper.writeValueAsString(msg);
		rabbitTemplate.convertAndSend(messageExchange.getName(), topic, json);
	}

	private ObjectMapper buildObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}

}