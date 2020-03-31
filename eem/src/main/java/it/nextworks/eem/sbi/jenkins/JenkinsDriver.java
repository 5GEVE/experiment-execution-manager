package it.nextworks.eem.sbi.jenkins;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.JobWithDetails;
import it.nextworks.eem.configuration.ConfigurationParameters;
import it.nextworks.eem.rabbitMessage.AbortingResultInternalMessage;
import it.nextworks.eem.rabbitMessage.InternalMessage;
import it.nextworks.eem.rabbitMessage.TestCaseResultInternalMessage;
import it.nextworks.eem.rabbitMessage.ValidationResultInternalMessage;
import it.nextworks.eem.sbi.interfaces.ExecutorServiceProviderInterface;
import it.nextworks.eem.sbi.interfaces.ValidatorServiceProviderInterface;
import it.nextworks.eem.sbi.rav.RAVDriver;
import it.nextworks.eem.sbi.rav.ValidationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class JenkinsDriver implements ExecutorServiceProviderInterface, ValidatorServiceProviderInterface {

    // static variable single_instance of type JenkinsDriver
    private static JenkinsDriver single_instance = null;

    private static final Logger log = LoggerFactory.getLogger(JenkinsDriver.class);

    private RabbitTemplate rabbitTemplate;
    private TopicExchange messageExchange;

    private JenkinsServer jenkinsServer;

    private String jenkinsValidationBaseUrl;

    // private constructor restricted to this class itself
    private JenkinsDriver(String jenkinsURI, String jenkinsUsername, String jenkinsPassword, String jenkinsValidationBaseUrl, RabbitTemplate rabbitTemplate, TopicExchange messageExchange) throws URISyntaxException{
        log.debug("Initializing Jenkins Driver : uri {}", jenkinsURI);
        jenkinsServer = new JenkinsServer(new URI(jenkinsURI), jenkinsUsername, jenkinsPassword);
        this.jenkinsValidationBaseUrl = jenkinsValidationBaseUrl;
        this.rabbitTemplate = rabbitTemplate;
        this.messageExchange = messageExchange;
    }

    // static method to create instance of JenkinsDriver class
    public static JenkinsDriver getInstance(String jenkinsURI, String jenkinsUsername, String jenkinsPassword, String jenkinsValidationBaseUrl, RabbitTemplate rabbitTemplate, TopicExchange messageExchange) throws URISyntaxException{
        if (single_instance == null)
            single_instance = new JenkinsDriver(jenkinsURI, jenkinsUsername, jenkinsPassword, jenkinsValidationBaseUrl, rabbitTemplate, messageExchange);
        else
            log.debug("Jenkins Driver already instantiated: uri {}", jenkinsURI);
        return single_instance;
    }

    //validation
    @Override
    public void configureExperiment(String executionId){
        //Validation is done by Jenkins during test case execution
        String topic = "lifecycle.validation." + executionId;
        InternalMessage internalMessage = new ValidationResultInternalMessage(ValidationStatus.CONFIGURED, "Validation done by Jenkins", false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageValidationError("Error while translating internal scheduling message in Json format", executionId);
        }
    }

    @Override
    public void startTcValidation(String executionId, String tcDescriptorId){
        String validationStarted = "OK";
        String topic = "lifecycle.validation." + executionId;
        InternalMessage internalMessage = new ValidationResultInternalMessage(ValidationStatus.ACQUIRING, validationStarted, false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageValidationError("Error while translating internal scheduling message in Json format", executionId);
        }
    }

    @Override
    public void stopTcValidation(String executionId, String tcDescriptorId){
        String validationStarted = "OK";
        String topic = "lifecycle.validation." + executionId;
        InternalMessage internalMessage = new ValidationResultInternalMessage(ValidationStatus.VALIDATING, validationStarted, false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageValidationError("Error while translating internal scheduling message in Json format", executionId);
        }
    }

    @Override
    public void queryValidationResult(String executionId, String tcDescriptorId){
        String reportUrl = this.jenkinsValidationBaseUrl + executionId + "/index.html";
        String topic = "lifecycle.validation." + executionId;
        InternalMessage internalMessage = new ValidationResultInternalMessage(ValidationStatus.VALIDATED, reportUrl, false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format");
            manageValidationError("Error while translating internal scheduling message in Json format", executionId);
        }
    }

    @Override
    public void terminateExperiment(String executionId){
        //nothing to do in this case
    }

    //execution
    @Override
    public void runTestCase(String executionId, String tcDescriptorId, String robotFile){//TODO change type of robotFile
        new Thread(() -> {
            runningJenkinsJob(executionId, tcDescriptorId, robotFile);
        }).start();
    }

    @Override
    public void abortTestCase(String executionId, String tcDescriptorId){
        new Thread(() -> {
            abortJenkinsJob(executionId, tcDescriptorId);
        }).start();
    }

    private void runningJenkinsJob(String executionId, String tcDescriptorId, String robotFile){
        String result = "";
        String name = "Execution_" + executionId + "__tcb_" + tcDescriptorId;
        log.debug("Running the experiment");
        log.debug("Getting the template file from resources");
        File configFile = getFileFromResources("job-template.xml");
        log.debug("Translating the received template to concrete configXML job file");
        String jenkinsJobDescription = createConfigXMLFileFromTemplate(configFile, executionId, tcDescriptorId, robotFile);
        log.debug("Creating a new jenkins job with executionId: {} and configFile {}", executionId, jenkinsJobDescription);
        try {
            jenkinsServer.createJob(name, jenkinsJobDescription);
        } catch(IOException e1) {
            log.error("Failed to create jenkins job {}", e1.getMessage());
            manageTestCaseError("Failed to create jenkins job", executionId, tcDescriptorId);
            return;
        }
        // RUN EXPERIMENT
        log.debug("Executing the experiment with executionId: " + executionId);
        try{
            jenkinsServer.getJob(name).build();
        } catch(IOException e2){
            log.error("Failed to build jenkins job with name {}. Error {}", name, e2.getMessage());
            manageTestCaseError("Failed to build jenkins job with name" + "Execution "+ executionId, executionId, tcDescriptorId);
            return;
        }
        // LOOP UNTIL TERMINATION IS DONE
        try{
            result = getJenkinsJobResult(name);
        } catch(IOException e3){
            log.error("Failed to retrieve jenkins job with name {} with error {}", "Execution "+ executionId, e3.getMessage());
            manageTestCaseError("Failed to retrieve jenkins job with name" + "Execution "+ executionId, executionId, tcDescriptorId);
            return;
        }

        switch(result){
            case "OK": manageTestCaseOK(result, executionId, tcDescriptorId); break;
            case "FAILED": manageTestCaseError(result, executionId, tcDescriptorId); break;
            case "ABORTED": manageTestCaseError(result, executionId, tcDescriptorId); break;
            default: manageTestCaseError(result, executionId, tcDescriptorId); break;
        }

    }

    private void abortJenkinsJob(String executionId, String tcDescriptorId) {
        //TODO abort test case

        //Check if Job is running (color has _anime)
        String name = "Execution_" + executionId + "__tcb_" + tcDescriptorId;
        JobWithDetails jobInfo = null;

        try{
            jobInfo = jenkinsServer.getJob(name);
        } catch (IOException e1){
            log.error("Failed to abort jenkins job with name {}", name);
            manageAbortingError("Failed to abort jenkins job with name" + name, executionId);
            return;
        }
        try {
            if (jobInfo.getLastBuild().details().isBuilding()) {
                jobInfo.getLastBuild().Stop();
            } else {
                log.error("Failed to abort jenkins job with name {} cause no running tasks are active", name);
                manageAbortingError("Failed to abort jenkins job with name {} cause no running tasks are active" + name, executionId);
            }
        } catch(IOException e1){
            log.error("Failed to abort jenkins job with name {} cause {}", name, e1.getMessage());
            manageAbortingError("Failed to abort jenkins job with name" + name, executionId);
        }
        try{
            jobInfo = jenkinsServer.getJob(name);
            if(jobInfo.getLastBuild().details().getResult().name().equalsIgnoreCase("ABORTED")){
                String result = "OK";
                String topic = "lifecycle.abortingResult." + executionId;
                InternalMessage internalMessage = new AbortingResultInternalMessage(result, false);
                try {
                    sendMessageToQueue(internalMessage, topic);
                } catch (JsonProcessingException e) {
                    log.error("Error while translating internal scheduling message in Json format");
                    manageAbortingError("Error while translating internal scheduling message in Json format", executionId);
                }
            } else {
                log.error("Failed to abort jenkins job with name {} ", name);
                manageAbortingError("Failed to abort jenkins job with name" + name, executionId);
            }
        } catch(IOException e2){
            log.error("Failed to abort jenkins job with name {} cause {}", name, e2.getMessage());
            manageAbortingError("Failed to abort jenkins job with name" + name, executionId);
        }

    }

    private void manageTestCaseOK(String result, String executionId, String tcDescriptorId){
        String topic = "lifecycle.testCaseResult." + executionId;
        InternalMessage internalMessage = new TestCaseResultInternalMessage(result, tcDescriptorId, false);
        try {
            sendMessageToQueue(internalMessage, topic);
        } catch (JsonProcessingException e) {
            log.error("Error while translating internal scheduling message in Json format {}", e.getMessage());

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

    private void manageValidationError(String errorMessage, String executionId){
        log.error("Validation of Experiment Execution with Id {} failed : {}", executionId, errorMessage);
        errorMessage = String.format("Validation of Experiment Execution with Id %s failed : %s", executionId, errorMessage);
        String topic = "lifecycle.validation." + executionId;
        InternalMessage internalMessage = new ValidationResultInternalMessage(ValidationStatus.FAILED, errorMessage, true);
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

    private String createConfigXMLFileFromTemplate(File template, String executionId, String tcDescriptorId,  String robotCode){
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
            log.error("Template file not found");
            e.printStackTrace();
        }
        log.debug("Generated Jenkins job file before substitution: {}", configXML );


        String lines[] = robotCode.split("\\|");

        String robotFileInConfig = "";
        for (int i = 0; i < lines.length; i++){
            log.debug("echo -e" + lines[i] + " >> ${WORKSPACE}/executionFile.robot");
            robotFileInConfig = robotFileInConfig.concat("echo \"" + lines[i] + "\" >> ${WORKSPACE}/executionFile.robot").concat("\n");
        }


        configXML = configXML.replace("__ROBOT_FILE__", robotFileInConfig);
        configXML = configXML.replace("_JOB__DESCRIPTION__","Job for experiment execution : " + executionId + " test case " + tcDescriptorId );
        configXML = configXML.replace("__EXECUTION_ID__", executionId);
        configXML = configXML.replace("__TCD_ID__", tcDescriptorId);


        log.debug("Generated Jenkins job file after substitution {}", configXML );
        return configXML;
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

    private String getJenkinsJobResult(String name) throws IOException {
        log.debug("Getting job details for jenkins job: " + name);
        //JobInfo jobInfo = jenkinsClient.api().jobsApi().jobInfo(null, name);
        JobWithDetails jobInfo = jenkinsServer.getJob(name);
        boolean isRunningJob = true;

        //TODO ADD Check if task is in queue
        while(jobInfo.isInQueue()){
            try{
                Thread.sleep(10000);
                jobInfo = jenkinsServer.getJob(name);
            } catch(InterruptedException e1){
                log.error(e1.getMessage());
                return "FAILED";
            }
        }
        log.debug("Looping till job is still running");
        while ( !jobInfo.hasFirstBuildRun() || jobInfo.getLastBuild().details().isBuilding()
        ) {
            log.debug("Not yet results for jenkins job {}", name);
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
            log.info("Job terminated correctly. Status of the job is {}", jobInfo.getLastBuild().details().getResult().name());
            return "OK";
        } else {
            log.info("Job terminated correctly. Status of the job is {}", jobInfo.getLastBuild().details().getResult().name());
            return "FAILED";
        }
    }

}
