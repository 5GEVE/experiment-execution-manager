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
import java.util.List;

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

import it.nextworks.eem.model.ConfigurationStatus;
import it.nextworks.eem.model.MetricInfo;
import it.nextworks.eem.rabbitMessage.AbortingResultInternalMessage;
import it.nextworks.eem.rabbitMessage.ConfigurationResultInternalMessage;
import it.nextworks.eem.rabbitMessage.InternalMessage;
import it.nextworks.eem.rabbitMessage.TestCaseResultInternalMessage;
import it.nextworks.eem.sbi.interfaces.ConfiguratorServiceProviderInterface;
import it.nextworks.eem.sbi.interfaces.ExecutorServiceProviderInterface;

public class RCDriver implements ConfiguratorServiceProviderInterface, ExecutorServiceProviderInterface {

	// static variable single_instance of type RCDriver
	private static RCDriver single_instance = null;

	private static final Logger log = LoggerFactory.getLogger(RCDriver.class);

	private RabbitTemplate rabbitTemplate;
	private TopicExchange messageExchange;
	private JenkinsServer jenkinsServer;

	// private constructor restricted to this class itself
	private RCDriver(String jenkinsURI, String jenkinsUsername, String jenkinsPassword, RabbitTemplate rabbitTemplate, TopicExchange messageExchange) throws URISyntaxException {
		log.debug("Initializing RC Driver : uri {}", "RC_URI");
		jenkinsServer = new JenkinsServer(new URI(jenkinsURI), jenkinsUsername, jenkinsPassword);
		this.rabbitTemplate = rabbitTemplate;
		this.messageExchange = messageExchange;
	}

	// static method to create instance of RCDriver class
	public static RCDriver getInstance(String jenkinsURI, String jenkinsUsername, String jenkinsPassword, RabbitTemplate rabbitTemplate, TopicExchange messageExchange) throws URISyntaxException {
		if (single_instance == null)
			single_instance = new RCDriver(jenkinsURI, jenkinsUsername, jenkinsPassword, rabbitTemplate, messageExchange);
		else
			log.debug("RC Driver already instantiated: uri {}", "RC_URI");
		return single_instance;
	}

	@Override
	public void applyConfiguration(String configId, String tcDescriptorId, String configScript){
		new Thread(() -> {applyConfigurationImplementation(configId, tcDescriptorId, configScript);}).start();
	}

	@Override
	public void abortConfiguration(String configId, String tcDescriptorId){
		new Thread(() -> {abortConfigurationImplementation(configId, tcDescriptorId);}).start();
	}

	@Override
	public void configureInfrastructureMetricCollection(String executionId, String tcDescriptorId, List<MetricInfo> metrics){
		new Thread(() -> {configureInfrastructureMetricCollectionImplementation(executionId, tcDescriptorId, metrics);}).start();
	}

	@Override
	public void resetConfiguration(String configId, String tcDescriptorId, String resetScript){
		new Thread(() -> {resetConfigurationImplementation(configId, tcDescriptorId, resetScript);}).start();
	}

	@Override
	public void removeInfrastructureMetricCollection(String executionId, String tcDescriptorId, List<String> metricConfigIds){
		new Thread(() -> {removeInfrastructureMetricCollectionImplementation(executionId, tcDescriptorId, metricConfigIds);}).start();
	}

	@Override
	public void runTestCase(String executionId, String tcDescriptorId, String testCaseFile){
		new Thread(() -> {runTestCaseImplementation(executionId, tcDescriptorId, testCaseFile);}).start();
	}

	@Override
	public void abortTestCase(String executionId, String tcDescriptorId){
		new Thread(() -> {abortTestCaseImplementation(executionId, tcDescriptorId);}).start();
	}

	private void applyConfigurationImplementation(String configId, String tcDescriptorId, String configScript){
		// Prepare CONFIGURATION
		log.debug("Starting configuration");
		String result = "";
		String name = "Configuration_" + configId + "__tcb_" + tcDescriptorId;

		log.debug("Getting the template file from resources");
		File configFile = getFileFromResources("job-template-conf.xml");

		log.debug("Translating the received template to concrete configXML job file");
		String jenkinsJobDescription = createConfigXMLFileFromTemplate(configFile, configId, tcDescriptorId, configScript);

		log.debug("Creating a new Jenkins job with configId: {} and configFile {}", configId, jenkinsJobDescription);
		try {
			jenkinsServer.createJob(name, jenkinsJobDescription);
		} catch(IOException e1) {
			log.error("Failed to create Jenkins job {}", e1.getMessage());
			manageConfigurationError("Failed to create Jenkins job", configId);
			return;
		}

		// Start CONFIGURATION
		log.debug("Running the configuration job with configId: " + configId);
		try{
			jenkinsServer.getJob(name).build();
		} catch(IOException e2){
			log.error("Failed to build Jenkins job with name {}. Error {}", name, e2.getMessage());
			manageConfigurationError("Failed to build Jenkins job with name" + "Configuration "+ configId, configId);
			return;
		}

		// Loop until CONFIGURATION job is done
		try{
			result = getJenkinsJobResult(name);
		} catch(IOException e3){
			log.error("Failed to retrieve Jenkins job with name {} with error {}", "Configuration "+ configId, e3.getMessage());
			manageConfigurationError("Failed to retrieve Jenkins job with name" + "Configuration " + configId, configId);
			return;
		}

		// Evaluation of results for CONFIGURATION job
		switch(result){
		case "OK": manageConfigurationOK(result, configId, tcDescriptorId); break;
		case "FAILED": manageConfigurationError("Jenkins job for Configuration task FAILED", configId); break;
		case "ABORTED": manageConfigurationError("Jenkins job for Configuration task was ABORTED", configId); break;
		default: manageConfigurationError("Status of Jenkins job for Configuration is UNKNOWN", configId); break;
		}
	}

	private void abortConfigurationImplementation(String executionId, String tcDescriptorId){
		//TODO abort configuration
		//no response message needed
	}

	private void configureInfrastructureMetricCollectionImplementation(String executionId, String tcDescriptorId, List<MetricInfo> metricst){
		//TODO remove
		try {
			log.debug("Configuring infrastructure metrics");
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			log.debug("Sleep error");
		}

		//TODO configure infrastructure metrics
		//metric configuration ok
		String result = "OK";
		List<String> metricConfigIds = new ArrayList<>();
		metricConfigIds.add("metric1_id");//TODO use IDs returned by RC
		String topic = "lifecycle.configurationResult." + executionId;
		InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.METRIC_CONFIGURED, result, metricConfigIds,false);
		try {
			sendMessageToQueue(internalMessage, topic);
		} catch (JsonProcessingException e) {
			log.error("Error while translating internal scheduling message in Json format");
			manageConfigurationError("Error while translating internal scheduling message in Json format", executionId);
		}

		//TODO handle metric configuration error
		//metric configuration ko
		//manageConfigurationError();
	}

	private void resetConfigurationImplementation(String executionId, String tcDescriptorId, String resetScript){
		//TODO reset configuration
		//no response message needed
	}

	private void removeInfrastructureMetricCollectionImplementation(String executionId, String tcDescriptorId, List<String> metricConfigIds){
		//TODO remove infrastructure metrics
		//no response message needed
	}

	private void runTestCaseImplementation(String executionId, String tcDescriptorId, String execScript){
		// Prepare EXPERIMENT EXECUTION
		log.debug("Starting experiment execution");
		String result = "";
		String name = "Execution_" + executionId + "__tcb_" + tcDescriptorId;

		log.debug("Getting the template file from resources");
		File executionFile = getFileFromResources("job-template-exec.xml");

		log.debug("Translating the received template to concrete configXML job file");
		String jenkinsJobDescription = createExecXMLFileFromTemplate(executionFile, executionId, tcDescriptorId, execScript);

		log.debug("Creating a new Jenkins job with executionId: {} and executionFile {}", executionId, jenkinsJobDescription);
		try {
			jenkinsServer.createJob(name, jenkinsJobDescription);
		} catch(IOException e1) {
			log.error("Failed to create Jenkins job {}", e1.getMessage());
			manageTestCaseError("Failed to create Jenkins job", executionId, tcDescriptorId);
			return;
		}

		// Start EXPERIMENT EXECUTION
		log.debug("Running the execution job with executionId: " + executionId);
		try{
			jenkinsServer.getJob(name).build();
		} catch(IOException e2){
			log.error("Failed to build Jenkins job with name {}. Error {}", name, e2.getMessage());
			manageTestCaseError("Failed to build Jenkins job with name" + "Execution "+ executionId, executionId, tcDescriptorId);
			return;
		}
		
		// Loop until EXPERIMENT EXECUTION job is done
		try{
			result = getJenkinsJobResult(name);
		} catch(IOException e3){
			log.error("Failed to retrieve Jenkins job with name {} with error {}", "Execution "+ executionId, e3.getMessage());
			manageTestCaseError("Failed to retrieve Jenkins job with name" + "Execution "+ executionId, executionId, tcDescriptorId);
			return;
		}

		// Evaluation of results for EXPERIMENT EXECUTION job
		switch(result){
		case "OK": manageExecutionOK(result, executionId, tcDescriptorId); break;
		case "FAILED": manageTestCaseError(result, executionId, tcDescriptorId); break;
		case "ABORTED": manageTestCaseError(result, executionId, tcDescriptorId); break;
		default: manageTestCaseError(result, executionId, tcDescriptorId); break;
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

	private File getFileFromResources(String fileName) {

		ClassLoader classLoader = getClass().getClassLoader();

		URL resource = classLoader.getResource(fileName);
		if (resource == null) {
			throw new IllegalArgumentException("file is not found!");
		} else {
			return new File(resource.getFile());
		}

	}

	private String createConfigXMLFileFromTemplate (File template, String configId, String tcDescriptorId, String configScript){

		String configXML = "";

		try (FileReader reader = new FileReader(template);
				BufferedReader br = new BufferedReader(reader)) {
			String line;
			while ((line = br.readLine()) != null) {
				log.debug("LINE: {}", line);
				configXML = configXML.concat(line);
			}
		} catch (FileNotFoundException e) {
			log.error("Template file not found");
			e.printStackTrace();
		} catch (IOException e) {
			log.error("IOExeption");
			e.printStackTrace();
		}

		log.debug("Generated Configuration Jenkins job file before substitution: {}", configXML );

		String lines[] = configScript.split("\\|");
		String robotFileInConfig = "";

		for (int i = 0; i < lines.length; i++){
			log.debug("echo -e" + lines[i] + " >> ${WORKSPACE}/configurationFile.robot");
			robotFileInConfig = robotFileInConfig.concat("echo '" + lines[i] + "' >> ${WORKSPACE}/configurationFile.robot").concat("\n");
		}

		configXML = configXML.replace("__ROBOT_FILE__", robotFileInConfig);
		configXML = configXML.replace("_JOB__DESCRIPTION__","Job for configuration : " + configId + " test case " + tcDescriptorId );

		log.debug("Generated Configuration Jenkins job file after substitution {}", configXML );

		return configXML;
	}

	private String createExecXMLFileFromTemplate (File template, String executionId, String tcDescriptorId, String execScript){
		
		String execXML = "";

		try (FileReader reader = new FileReader(template);
				BufferedReader br = new BufferedReader(reader)) {
			String line;
			while ((line = br.readLine()) != null) {
				log.debug("LINE: {}", line);
				execXML = execXML.concat(line);
			}
		} catch (FileNotFoundException e) {
			log.error("Template file not found");
			e.printStackTrace();
		} catch (IOException e) {
			log.error("IOExeption");
			e.printStackTrace();
		}

		log.debug("Generated Execution Jenkins job file before substitution: {}", execXML );

		String lines[] = execScript.split("\\|");
		String robotFileInExec = "";

		for (int i = 0; i < lines.length; i++){
			log.debug("echo -e" + lines[i] + " >> ${WORKSPACE}/executionFile.robot");
			robotFileInExec = robotFileInExec.concat("echo '" + lines[i] + "' >> ${WORKSPACE}/executionFile.robot").concat("\n");
		}

		execXML = execXML.replace("__ROBOT_FILE__", robotFileInExec);
		execXML = execXML.replace("_JOB__DESCRIPTION__","Job for experiment execution : " + executionId + " test case " + tcDescriptorId );

		log.debug("Generated Execution Jenkins job file after substitution {}", execXML );

		return execXML;
	}

	private String getJenkinsJobResult(String name) throws IOException {
		
		log.debug("Getting job details for jenkins job: " + name);
		JobWithDetails jobInfo = jenkinsServer.getJob(name);

		//Sleep while job is in queue
		while(jobInfo.isInQueue()){
			try{
				log.debug("Job is in queue");
				Thread.sleep(10000);
				jobInfo = jenkinsServer.getJob(name);
			} catch(InterruptedException e1){
				log.error(e1.getMessage());
				return "FAILED";
			}
		}

		log.debug("Job has started running");
		while ( !jobInfo.hasFirstBuildRun() || jobInfo.getLastBuild().details().isBuilding()) {
			log.debug("Results not yet available for jenkins job {}", name);
			try{
				Thread.sleep(30000);
			} catch(InterruptedException e2){
				log.error(e2.getMessage());
				return "FAILED";
			}
			jobInfo = jenkinsServer.getJob(name);
		}

		// In case of aborted job
		if (jobInfo.getLastBuild().details().getResult().name().equalsIgnoreCase("ABORTED")) {
			log.info("Job was aborted");
			return "ABORTED";
		}

		// When job is done, notification is sent to the EEM
		if (jobInfo.getLastBuild().details().getResult().name().equalsIgnoreCase("SUCCESS") ) {
			log.info("Job terminated. Status of the job is {}", jobInfo.getLastBuild().details().getResult().name());
			return "OK";
		} else {
			log.info("Job terminated. Status of the job is {}", jobInfo.getLastBuild().details().getResult().name());
			return "FAILED";
		}
	}

	private void manageConfigurationOK (String result, String configId, String tcDescriptorId) {
		String topic = "lifecycle.configurationResult." + configId;
		InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.CONFIGURED, result, null, false);
		try {
			sendMessageToQueue(internalMessage, topic);
		} catch (JsonProcessingException e) {
			log.error("Error while translating internal scheduling message in Json format");
			manageConfigurationError("Error while translating internal scheduling message in Json format", configId);
		}
	}

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

	private void manageConfigurationError(String errorMessage, String executionId){
		log.error("Configuration of Experiment Execution with Id {} failed : {}", executionId, errorMessage);
		errorMessage = String.format("Configuration of Experiment Execution with Id %s failed : %s", executionId, errorMessage);
		String topic = "lifecycle.configurationResult." + executionId;
		InternalMessage internalMessage = new ConfigurationResultInternalMessage(ConfigurationStatus.FAILED, errorMessage, null, true);
		try {
			sendMessageToQueue(internalMessage, topic);
		} catch (JsonProcessingException e) {
			log.error("Error while translating internal scheduling message in Json format");
			log.debug(null, e);
		}
	}

	private void manageTestCaseError(String errorMessage, String executionId, String tcDescriptorId){
		log.error("Test Case with Id {} of Experiment Execution with Id {} failed : {}", tcDescriptorId, executionId, errorMessage);
		errorMessage = String.format("Test Case with Id %s for Experiment Execution with Id %s failed : %s", tcDescriptorId, executionId, errorMessage);
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