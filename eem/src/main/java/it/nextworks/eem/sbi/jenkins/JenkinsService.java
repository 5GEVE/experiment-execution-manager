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
import it.nextworks.eem.sbi.expcatalogue.ExperimentCatalogueRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Service
@ConditionalOnProperty(
        value="eem.sbi.service.jenkins",
        havingValue = "true",
        matchIfMissing = true)
public class JenkinsService {

    private static final Logger log = LoggerFactory.getLogger(JenkinsService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${eem.jenkins.uri}")
    private String jenkinsURI;


    @Value("${eem.jenkins.username}")
    private String jenkinsUsername;


    @Value("${eem.jenkins.password}")
    private String jenkinsPassword;

    private JenkinsServer jenkinsServer;

    @Autowired
    @Qualifier(ConfigurationParameters.eemQueueExchange)
    private TopicExchange messageExchange;

    public JenkinsService() throws URISyntaxException {

    }

    @PostConstruct
    private void initJenkinsService() throws URISyntaxException {
        log.debug("Initializing Jenkins Service");
        log.debug("################### {}, ###################### {}, ##################### {}",
                jenkinsURI,
                jenkinsUsername,
                jenkinsPassword);
        jenkinsServer = new JenkinsServer(new URI(jenkinsURI), jenkinsUsername, jenkinsPassword);
    }


    public void runTestCase(String executionId, String tcDescriptorId, String robotFile){//TODO change type of robotFile
        new Thread(() -> {
            runningJenkinsJob(executionId, tcDescriptorId, robotFile);
        }).start();
    }

    public void abortTestCase(String executionId, String tcDescriptorId){
        new Thread(() -> {
            abortJenkinsJob(executionId, tcDescriptorId);
        }).start();
    }

    private void runningJenkinsJob(String executionId, String tcDescriptorId, String robotFile){
        String result = "";
        log.debug("Running the experiment");
        log.debug("Getting the template file from resources");
        File configFile = getFileFromResources("job-template.xml");
        log.debug("Translating the received template to concrete configXML job file");
        String jenkinsJobDescription = createConfigXMLFileFromTemplate(configFile, executionId, robotFile);
        log.debug("Creating a new jenkins job with executionId: " + executionId);
        try {
            jenkinsServer.createJob("Execution "+ executionId, jenkinsJobDescription);
        } catch(IOException e1) {
            log.error("Failed to create jenkins job");
            manageTestCaseError("Failed to create jenkins job", executionId, tcDescriptorId);
            return;
        }
        // RUN EXPERIMENT
        log.debug("Executing the experiment with executionId: " + executionId);
        try{
            jenkinsServer.getJob("Execution "+ executionId).build();
        } catch(IOException e2){
            log.error("Failed to build jenkins job with name {}", "Execution "+ executionId);
            manageTestCaseError("Failed to build jenkins job with name" + "Execution "+ executionId, executionId, tcDescriptorId);
            return;
        }
        // LOOP UNTIL TERMINATION IS DONE
        try{
            result = getJenkinsJobResult("Execution "+ executionId);
        } catch(IOException e3){
            log.error("Failed to retrieve jenkins job with name {}", "Execution "+ executionId);
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
        String name = "Execution "+ executionId;
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

    private String createConfigXMLFileFromTemplate(File template, String name,  String robotCode){
        String configXML = "";

        try (FileReader reader = new FileReader(template);
             BufferedReader br = new BufferedReader(reader)) {

            String line;
            while ((line = br.readLine()) != null) {
                configXML.concat(line);
            }
        } catch (FileNotFoundException e) {
            log.error("Template file not found");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        configXML = configXML.replace("__ROBOT_FILE__", robotCode);
        configXML = configXML.replace("_JOB__DESCRIPTION__","Job for experiment: " + name );
        configXML = configXML.replace("__EXECUTION_ID__", name);

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
