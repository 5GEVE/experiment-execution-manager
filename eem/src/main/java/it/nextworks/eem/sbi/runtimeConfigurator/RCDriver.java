package it.nextworks.eem.sbi.runtimeConfigurator;


import java.net.URISyntaxException;
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
		log.debug("Initializing RC Driver: URI {}", rcURI);
		
		ApiClient apiClient = new ApiClient();
		apiClient.setBasePath (rcURI);
		apiClient.setConnectTimeout(60000);
		apiClient.setReadTimeout(60000);
		rcApi = new RcnbiControllerApi (apiClient);
		
		this.rabbitTemplate = rabbitTemplate;
		this.messageExchange = messageExchange;
	}

	// static method to create instance of RCDriver class
	public static RCDriver getInstance(String rcURI, RabbitTemplate rabbitTemplate, TopicExchange messageExchange) throws URISyntaxException {
		if (single_instance == null)
			single_instance = new RCDriver(rcURI, rabbitTemplate, messageExchange);
		else
			log.debug("RC Driver already instantiated: URI {}", rcURI);
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
		log.debug("Day-2 Configuration INIT. Initializing day-2 configuration task for Test Case {} with executionId {}", tcDescriptorId, executionId);
		String result = "";
		String configId = "";
		//TODO: remove this approach (in all three processes), since the EEM gives the config/exec Id when needed
		String experiments_key = "config_" + tcDescriptorId + "_" + executionId;
								
		// PROCESS: Request configId from RC
		log.debug("Request configId from RC. Requesting configId for Test Case {} with executionId {}", tcDescriptorId, executionId);
		ApplicationDay2ConfigurationWrapper day2Wrapper = new ApplicationDay2ConfigurationWrapper();
		day2Wrapper.setConfigurationScript (configScript);
		day2Wrapper.setResetConfigScript (resetScript);

		try {
			ApplicationDay2ConfigurationResponse configIdResponse = rcApi.applicationDay2ConfigurationInit(day2Wrapper);
			configId = configIdResponse.getConfigurationId();
			log.debug("Request configId from RC. configId for Test Case {} with executionId {} is: {}", tcDescriptorId, executionId, configId);

			// we associate the configId with the specific ELM-provided ID; this is needed for the reset method
			experiments.put(experiments_key, configId);

		} catch (ApiException e1) {
			log.error("Request configId from RC. ApiException for Test Case {} with executionId {}", tcDescriptorId, executionId, e1);
			manageConfigurationError("EEM: API Failure while requesting configId for application day-2 configuration", executionId);
			return;
		}

		// PROCESS: Start day-2 configuration
		log.debug("Start day-2 configuration. Starting configuration process for Test Case {} with executionId {}", tcDescriptorId, executionId);
		boolean isConfiguring = true;
		
		try {
			ApplicationDay2ConfigurationStatusResponse startResponse = rcApi.applicationDay2ConfigurationStart(configId);
			
			switch (startResponse.getStatus()) {
				case COMPLETED: result = "OK"; isConfiguring = false; break;
				case ABORTED: result = "ABORTED"; isConfiguring = false; break;
				case FAILED: result = "FAILED"; isConfiguring = false; break;
				default:;
			}
			
		} catch (ApiException e2) {
			log.error("Start day-2 configuration. ApiException for Test Case {} with executionId {}", tcDescriptorId, executionId, e2);
			manageConfigurationError("EEM: API Failure while starting application day-2 configuration", executionId);
			return;
		}		

		// PROCESS: Day-2 configuration loop
		try {
			while (isConfiguring) {
				ApplicationDay2ConfigurationStatusResponse statusResponse = rcApi.applicationDay2ConfigurationStatus(configId);
				
				switch (statusResponse.getStatus()) {
					case COMPLETED: result = "OK"; isConfiguring = false; break;
					case ABORTED: result = "ABORTED"; isConfiguring = false; break;
					case FAILED: result = "FAILED"; isConfiguring = false; break;
					default: Thread.sleep(3000);
				}
			}
		} catch (ApiException e3) {
			log.error("Day-2 configuration loop. ApiException for Test Case {} with executionId {}", tcDescriptorId, executionId, e3);
			manageConfigurationError("EEM: API Failure while waiting for application day-2 configuration", executionId);
			return;
		} catch (InterruptedException e4) {
			log.error("Day-2 configuration loop. InterruptedException for Test Case {} with executionId {}", tcDescriptorId, executionId, e4);
			manageConfigurationError("EEM: Interruption while waiting for application day-2 configuration", executionId);
			return;
		}

		log.debug("Day-2 configuration loop. Configuration process for Test Case {} with executionId {} finished with result {}", tcDescriptorId, executionId, result);
		
		// Evaluation of results for configuration job
		switch (result) {
			case "OK": manageConfigurationOK(result, executionId, tcDescriptorId, configId); break;
			case "ABORTED": manageConfigurationError("RC: Day-2 Configuration task was ABORTED", executionId); break;
			case "FAILED": manageConfigurationError("RC: Day-2 Configuration task FAILED", executionId); break;
			default: manageConfigurationError("RC: Status for Day-2 Configuration is UNKNOWN", executionId); break;
		}
	}

	private void resetConfigurationImplementation(String executionId, String tcDescriptorId, String configId){
		
		// PROCESS: Day-2 Configuration reset INIT
		log.debug("Day-2 Configuration RESET. Initializing day-2 configuration reset for Test Case {} with executionId {}", tcDescriptorId, executionId);
		String experiments_key = "config_" + tcDescriptorId + "_" + executionId;
		//String configId = experiments.get(experiments_key); // we retrieve the configId from experiments
		String result = "";

		// PROCESS: Reset day-2 configuration
		log.debug("Reset day-2 configuration. Starting reset process for Test Case {} with executionId {}", tcDescriptorId, executionId);
		boolean isReseting = true;
				
		try {
			ApplicationDay2ConfigurationStatusResponse resetResponse = rcApi.applicationDay2ConfigurationReset(configId);
			
			switch (resetResponse.getStatus()) {
				case CLEANED: result = "OK"; isReseting = false; break;
				case ABORTED: result = "ABORTED"; isReseting = false; break;
				case FAILED: result = "FAILED"; isReseting = false; break;
				default:;
			}
		} catch (ApiException e1) {
			log.error("Start day-2 configuration. ApiException for Test Case {} with executionId {}", tcDescriptorId, executionId, e1);
			manageConfigurationError("EEM: API Failure while starting application day-2 configuration", executionId);
			return;
		}	
		
		// PROCESS: Reset day-2 configuration loop
		try {
			while (isReseting) {
				ApplicationDay2ConfigurationStatusResponse statusResponse = rcApi.applicationDay2ConfigurationStatus(configId);
				
				switch (statusResponse.getStatus()) {
					case CLEANED: result = "OK"; isReseting = false; break;
					case ABORTED: result = "ABORTED"; isReseting = false; break;
					case FAILED: result = "FAILED"; isReseting = false; break;
					default: Thread.sleep(3000);
				}
			}
		} catch (ApiException e2) {
			log.error("Reset day-2 configuration. ApiException for Test Case {} with executionId {}", tcDescriptorId, executionId, e2);
			manageConfigurationError("EEM: API Failure while resetting application day-2 configuration", executionId);
			return;
		} catch (InterruptedException e3) {
			log.error("Reset day-2 configuration. InterruptedException for Test Case {} with executionId {}", tcDescriptorId, executionId, e3);
			manageConfigurationError("EEM: Interruption while resetting application day-2 configuration", executionId);
			return;
		}
		
		log.debug("Reset day-2 configuration loop. Configuration reset for Test Case {} with executionId {} finished with result {}", tcDescriptorId, executionId, result);
	
		// we remove the configuration job from the list of jobs
		experiments.remove(experiments_key, configId);

		// Evaluation of results for configuration reset job
		switch (result) {
			case "OK":
				String topic = "lifecycle.configurationResult." + executionId;
				InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.CONF_RESET, result, null,false);
				try {
					sendMessageToQueue(internalMessage, topic);
				} catch (JsonProcessingException e) {
					log.error("Error while translating internal scheduling message in JSON format", e);
					manageConfigurationError("EEM: Error while translating internal scheduling message in Json format", executionId);
				}
				break;
			case "ABORTED": manageConfigurationError("RC: Day-2 Configuration reset task was ABORTED", executionId); break;
			case "FAILED": manageConfigurationError("RC: Day-2 Configuration reset task FAILED", executionId); break;
			default: manageConfigurationError("RC: Status for Day-2 Configuration reset is UNKNOWN", executionId); break;
		}

	}

	private void abortConfigurationImplementation(String executionId, String tcDescriptorId, String configId){
		//TODO abort configuration
		//no response message needed
	}

	private void configureInfrastructureMetricCollectionImplementation(String executionId, String tcDescriptorId, List<MetricInfo> metrics){

		// PROCESS: Infrastructure Metrics Configuration INIT
		log.debug("Infrastructure Metrics Configuration INIT. Initializing metrics configuration task for Test Case {} with executionId {}", tcDescriptorId, executionId);
		String result = "";
		String metricsConfigId = "";
		// TODO: apparently this will not be needed since we get the metricsConfigId in the removal --> confirm and remove
		String experiments_key = "metricsConfig_" + tcDescriptorId + "_" + executionId;

		// PROCESS: Request metricsConfigId from RC
		log.debug("Request metricsConfigId from RC. Requesting metricsConfigId for Test Case {} with executionId {}", tcDescriptorId, executionId);

		int numberOfMetrics = metrics.size();
		InfrastructureDay2ConfigurationWrapper metricsWrapper = new InfrastructureDay2ConfigurationWrapper();
		List<InfrastructureMetricWrapper> metricsList = new ArrayList<>();

		for (int i = 0; i < numberOfMetrics; i++) {
			InfrastructureMetricWrapper metricWrapper = new InfrastructureMetricWrapper();

			String metricId = metrics.get(i).getMetric().getMetricId();
			metricWrapper.setMetricId(metricId);
			String unit = metrics.get(i).getMetric().getUnit();
			metricWrapper.setUnit(unit);
			String interval = metrics.get(i).getMetric().getInterval();
			metricWrapper.setInterval(interval);
			String topic = metrics.get(i).getTopic();
			metricWrapper.setTopic(topic);
			String site = metrics.get(i).getTargetSite().toString();
			metricWrapper.setSite(site);
			String metricType = metrics.get(i).getMetric().getiMetricType().toString();
			metricWrapper.setMetricType(metricType);
			metricsList.add(metricWrapper);
		}

		metricsWrapper.setInfrastructureMetricsInfo(metricsList);

		try {
			InfrastructureDay2ConfigurationResponse metricsConfigIdResponse = rcApi.infrastructureDay2ConfigurationInit(metricsWrapper);
			metricsConfigId = metricsConfigIdResponse.getConfigurationId();
			log.debug("Request metricsConfigId from RC. metricsConfigId for Test Case {} with executionId {} is: {}", tcDescriptorId, executionId, metricsConfigId);

			// TODO: we associate the metricsConfigId with the specific ELM-provided ID --> confirm this is not needed for the reset method and remove
			experiments.put(experiments_key, metricsConfigId);

		} catch (ApiException e1) {
			log.error("Request metricsConfigId from RC. ApiException for Test Case {} with executionId {}", tcDescriptorId, executionId, e1);
			manageConfigurationError("EEM: API Failure while requesting metricsConfigId for infrastructure metrics configuration", executionId);
			return;
		}

		// PROCESS: Start metrics configuration
		log.debug("Start metrics configuration. Starting metrics configuration process for Test Case {} with executionId {}", tcDescriptorId, executionId);
		boolean isConfiguring = true;

		try {
			InfrastructureDay2ConfigurationStatusResponse startResponse = rcApi.infrastructureDay2ConfigurationStart(metricsConfigId);

			switch (startResponse.getStatus()) {
				case COMPLETED: result = "OK"; isConfiguring = false; break;
				case ABORTED: result = "ABORTED"; isConfiguring = false; break;
				case FAILED: result = "FAILED"; isConfiguring = false; break;
				default:;
			}

		} catch (ApiException e2) {
			log.error("Start metrics configuration. ApiException for Test Case {} with executionId {}", tcDescriptorId, executionId, e2);
			manageConfigurationError("EEM: API Failure while starting infrastructure metrics configuration", executionId);
			return;
		}		

		// PROCESS: Metrics configuration loop
		try {
			while (isConfiguring) {
				InfrastructureDay2ConfigurationStatusResponse statusResponse = rcApi.infrastructureDay2ConfigurationStatus(metricsConfigId);

				switch (statusResponse.getStatus()) {
					case COMPLETED: result = "OK"; isConfiguring = false; break;
					case ABORTED: result = "ABORTED"; isConfiguring = false; break;
					case FAILED: result = "FAILED"; isConfiguring = false; break;
					default: Thread.sleep(3000);
				}
			}
		} catch (ApiException e3) {
			log.error("Metrics configuration loop. ApiException for Test Case {} with executionId {}", tcDescriptorId, executionId, e3);
			manageConfigurationError("EEM: API Failure while waiting for infrastructure metrics configuration", executionId);
			return;
		} catch (InterruptedException e4) {
			log.error("Metrics configuration loop. InterruptedException for Test Case {} with executionId {}", tcDescriptorId, executionId, e4);
			manageConfigurationError("EEM: Interruption while waiting for infrastructure metrics configuration", executionId);
			return;
		}

		log.debug("Metrics configuration loop. Metrics configuration process for Test Case {} with executionId {} finished with result {}", tcDescriptorId, executionId, result);

		// Evaluation of results for metrics configuration job
		switch (result){
			case "OK":
				// so far there is a single metricsConfigId that relates to all metrics. TODO: evaluate if this needs change
				//List<String> metricConfigIds = new ArrayList<>();
				//metricConfigIds.add("metricsConfigId");
				String topic = "lifecycle.configurationResult." + executionId;
				InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.METRIC_CONFIGURED, result, metricsConfigId, false);
				try {
					sendMessageToQueue(internalMessage, topic);
				} catch (JsonProcessingException e) {
					log.error("Error while translating internal scheduling message in JSON format", e);
					manageConfigurationError("EEM: Error while translating internal scheduling message in Json format", executionId);
				}
				break;
			case "ABORTED": manageConfigurationError("RC: Metrics Configuration task was ABORTED", executionId); break;
			case "FAILED": manageConfigurationError("RC: Metrics Configuration task FAILED", executionId); break;
			default: manageConfigurationError("RC: Status for Metrics Configuration is UNKNOWN", executionId); break;
		}

	}

	// TODO: in this method, metricsConfigId is fine since it is returned by the config method. Still, it refers to all metrics at the same time (today)
	private void removeInfrastructureMetricCollectionImplementation(String executionId, String tcDescriptorId, String metricsConfigId){

		// PROCESS: Metrics stop INIT
		log.debug("Metrics stop INIT. Initializing infrastructure metrics stop for Test Case {} with executionId {}", tcDescriptorId, executionId);
		String result = "";

		// PROCESS: Stop metrics
		log.debug("Stop metrics. Stopping infrastructure metrics for Test Case {} with executionId {}", tcDescriptorId, executionId);
		boolean isReseting = true;

		try {
			InfrastructureDay2ConfigurationStatusResponse stopResponse = rcApi.infrastructureDay2ConfigurationStop(metricsConfigId);

			switch (stopResponse.getStatus()) {
				case STOPPED: result = "OK"; isReseting = false; break;
				case ABORTED: result = "ABORTED"; isReseting = false; break;
				case FAILED: result = "FAILED"; isReseting = false; break;
				default:;
			}
		} catch (ApiException e1) {
			log.error("Stop metrics. ApiException for Test Case {} with executionId {}", tcDescriptorId, executionId, e1);
			manageConfigurationError("EEM: API Failure while requesting to stop infrastructure metrics", executionId);
			return;
		}	

		// PROCESS: Stop metrics loop
		try {
			while (isReseting) {
				InfrastructureDay2ConfigurationStatusResponse statusResponse = rcApi.infrastructureDay2ConfigurationStatus(metricsConfigId);

				switch (statusResponse.getStatus()) {
					case STOPPED: result = "OK"; isReseting = false; break;
					case ABORTED: result = "ABORTED"; isReseting = false; break;
					case FAILED: result = "FAILED"; isReseting = false; break;
					default: Thread.sleep(3000);
				}
			}
		} catch (ApiException e2) {
			log.error("Stop metrics loop. ApiException for Test Case {} with executionId {}", tcDescriptorId, executionId, e2);
			manageConfigurationError("EEM: API Failure while waiting to stop infrastructure metrics", executionId);
			return;
		} catch (InterruptedException e3) {
			log.error("Stop metrics loop. InterruptedException for Test Case {} with executionId {}", tcDescriptorId, executionId, e3);
			manageConfigurationError("EEM: Interruption while waiting to stop infrastructure metrics", executionId);
			return;
		}

		log.debug("Stop metrics. Metrics for Test Case {} with executionId {} stopped with result {}", tcDescriptorId, executionId, result);

		// Evaluation of results for configuration reset job
		switch (result) {
			case "OK":
				String topic = "lifecycle.configurationResult." + executionId;
				InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.METRIC_RESET, result, null,false);
				try {
					sendMessageToQueue(internalMessage, topic);
				} catch (JsonProcessingException e) {
					log.error("Error while translating internal scheduling message in Json format", e);
					manageConfigurationError("EEM: Error while translating internal scheduling message in JSON format", executionId);
				}
				break;
			case "ABORTED": manageConfigurationError("RC: Metrics stop task was ABORTED", executionId); break;
			case "FAILED": manageConfigurationError("RC: Metrics stop task FAILED", executionId); break;
			default: manageConfigurationError("RC: Status for metrics stop task is UNKNOWN", executionId); break;
		}
	}

	private void runTestCaseImplementation(String executionId, String tcDescriptorId, String execScript){
		
		// PROCESS: Experiment Execution INIT
		log.debug("Experiment Execution INIT. Initializing experiment execution task for Test Case {} with executionId {}", tcDescriptorId, executionId);
		String result = "";
		String execId = "";
		
		// PROCESS: Request execId from RC
		log.debug("Request execId from RC. Requesting execId for Test Case {} with executionId {}", tcDescriptorId, executionId);
		ExecutionWrapper execWrapper = new ExecutionWrapper();
		execWrapper.setExecutionScript (execScript);
		
		try {
			ExecutionResponse execIdResponse = rcApi.executionInit(execWrapper);
			execId = execIdResponse.getExecutionId();
			log.debug("Request execId from RC. execId for Test Case {} with executionId {} is: {}", tcDescriptorId, executionId, execId);
		} catch (ApiException e1) {
			log.error("Request execId from RC. ApiException for Test Case {} with executionId {}", tcDescriptorId, executionId, e1);
			manageTestCaseError("EEM: API Failure while requesting execId for experiment execution", executionId, tcDescriptorId);
			return;
		}

		// PROCESS: Start experiment execution
		log.debug("Start experiment execution. Starting execution process for Test Case {} with executionId {}", tcDescriptorId, executionId);
		boolean isRunning = true;

		try {
			ExecutionStatusResponse startResponse = rcApi.executionStart(execId);

			switch (startResponse.getStatus()) {
				case COMPLETED: result = "OK"; isRunning = false; break;
				case ABORTED: result = "ABORTED"; isRunning = false; break;
				case FAILED: result = "FAILED"; isRunning = false; break;
				default:;
			}

		} catch (ApiException e2) {
			log.error("Start experiment execution. ApiException for Test Case {} with executionId {}", tcDescriptorId, executionId, e2);
			manageTestCaseError("EEM: API Failure while starting execution", executionId, tcDescriptorId);
			return;
		}		

		// PROCESS: Experiment execution loop
		try {
			while (isRunning) {
				ExecutionStatusResponse statusResponse = rcApi.executionStatus(execId);

				switch (statusResponse.getStatus()) {
					case COMPLETED: result = "OK"; isRunning = false; break;
					case ABORTED: result = "ABORTED"; isRunning = false; break;
					case FAILED: result = "FAILED"; isRunning = false; break;
					default: Thread.sleep(3000);
				}
			}
		} catch (ApiException e3) {
			log.error("Experiment execution loop. ApiException for Test Case {} with executionId {}", tcDescriptorId, executionId, e3);
			manageTestCaseError("EEM: API Failure while waiting for experiment execution", executionId, tcDescriptorId);
			return;
		} catch (InterruptedException e4) {
			log.error("Experiment execution loop. InterruptedException for Test Case {} with executionId {}", tcDescriptorId, executionId, e4);
			manageTestCaseError("EEM: Interruption while waiting for experiment execution", executionId, tcDescriptorId);
			return;
		}

		log.debug("Experiment execution loop. Experiment execution process for Test Case {} with executionId {} finished with result {}", tcDescriptorId, executionId, result);

		// Evaluation of results for experiment execution job
		switch (result) {
			case "OK": manageExecutionOK(result, executionId, tcDescriptorId); break;
			case "ABORTED": manageTestCaseError("RC: Experiment execution task was ABORTED", executionId, tcDescriptorId); break;
			case "FAILED": manageTestCaseError("RC: Experiment execution task FAILED", executionId, tcDescriptorId); break;
			default: manageTestCaseError("RC: Status for experiment execution is UNKNOWN", executionId, tcDescriptorId); break;
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
			log.error("Error while translating internal scheduling message in Json format", e);
			manageAbortingError("EEM: Error while translating internal scheduling message in Json format", executionId);
		}

		//TODO handle aborting error
		//aborting ko
		//manageAbortingError();
	}

	private void manageConfigurationOK (String result, String executionId, String tcDescriptorId, String configId) {
		String topic = "lifecycle.configurationResult." + executionId;
		InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.CONFIGURED, result, configId, false);
		try {
			sendMessageToQueue(internalMessage, topic);
		} catch (JsonProcessingException e) {
			log.error("Error while translating internal scheduling message in JSON format", e);
			manageConfigurationError("EEM: Error while translating internal scheduling message in Json format", executionId);
		}
	}

	private void manageExecutionOK (String result, String executionId, String tcDescriptorId) {
		String topic = "lifecycle.executionResult." + executionId;
		InternalMessage internalMessage = new TestCaseResultInternalMessage(result, tcDescriptorId, false);
		try {
			sendMessageToQueue(internalMessage, topic);
		} catch (JsonProcessingException e) {
			log.error("Error while translating internal scheduling message in Json format", e);
			manageConfigurationError("EEM: Error while translating internal scheduling message in Json format", executionId);
		}
	}

	private void manageConfigurationError(String errorMessage, String executionId){
		log.error("Configuration of Experiment Execution with Id {} failed: {}", executionId, errorMessage);
		errorMessage = String.format("Configuration of Experiment Execution with Id %s failed: %s", executionId, errorMessage);
		String topic = "lifecycle.configurationResult." + executionId;
		InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.FAILED, errorMessage, null, true);
		try {
			sendMessageToQueue(internalMessage, topic);
		} catch (JsonProcessingException e) {
			log.error("Error while translating internal scheduling message in JSON format", e);
		}
	}

	private void manageTestCaseError(String errorMessage, String executionId, String tcDescriptorId){
		log.error("Test Case with Id {} of Experiment Execution with Id {} failed: {}", tcDescriptorId, executionId, errorMessage);
		errorMessage = String.format("Test Case with Id %s for Experiment Execution with Id %s failed: %s", tcDescriptorId, executionId, errorMessage);
		String topic = "lifecycle.testCaseResult." + executionId;
		InternalMessage internalMessage = new TestCaseResultInternalMessage(errorMessage, tcDescriptorId, true);
		try {
			sendMessageToQueue(internalMessage, topic);
		} catch (JsonProcessingException e) {
			log.error("Error while translating internal scheduling message in Json format", e);
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
			log.error("Error while translating internal scheduling message in Json format", e);
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
