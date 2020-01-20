package it.nextworks.eem.engine;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.FolderJob;
import it.nextworks.eem.model.*;
import it.nextworks.eem.model.enumerate.ExperimentState;
import it.nextworks.eem.model.enumerate.SubscriptionType;
import it.nextworks.eem.repo.ExperimentExecutionRepository;
import it.nextworks.eem.repo.ExperimentExecutionSubscriptionRepository;
import it.nextworks.eem.sbi.jenkins.JenkinsService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class EEMServiceTests {

    private static final Logger log = LoggerFactory.getLogger(EEMServiceTests.class);

    @Autowired
    EemService eemService;

    @Autowired
    JenkinsService jenkinsService;

    @Autowired
    ExperimentExecutionRepository experimentExecutionRepository;

    @Autowired
    ExperimentExecutionSubscriptionRepository experimentExecutionSubscriptionRepository;

    @Value("${eem.jenkins.uri}")
    private String jenkinsUri;


    @Value("${eem.jenkins.username}")
    private String jenkinsUsername;


    @Value("${eem.jenkins.password}")
    private String jenkinsPassword;


    @Test
    @Ignore
    public void storeAndDeleteExperimentExecutionTest() throws Exception{
        ExperimentExecution experimentExecution = new ExperimentExecution();
        String executionId = UUID.randomUUID().toString();
        //testCaseDescriptorConfigurations
        List<TestCaseExecutionConfiguration> testCaseExecutionConfigurations = new ArrayList<>();
        Map<String, String> execConfiguration = new HashMap<>();
        execConfiguration.put("conf1", "conf1");
        TestCaseExecutionConfiguration testCase1 = new TestCaseExecutionConfiguration();
        testCase1.tcDescriptorId("testCaseDescriptor")
                .execConfiguration(execConfiguration);
        testCaseExecutionConfigurations.add(testCase1);
        TestCaseExecutionConfiguration testCase2 = new TestCaseExecutionConfiguration();
        testCase2.tcDescriptorId("testCaseDescriptor")
                .execConfiguration(execConfiguration);
        testCaseExecutionConfigurations.add(testCase2);
        //testCaseResult
        Map<String, ExecutionResult> testCaseResults = new HashMap<>();
        ExecutionResult executionResult = new ExecutionResult();
        executionResult.result("result1");
        testCaseResults.put("testCaseResult1", executionResult);
        experimentExecution.executionId(executionId)
                .state(ExperimentState.INIT)
                .testCaseDescriptorConfiguration(testCaseExecutionConfigurations)
                .testCaseResult(testCaseResults)
                .reportUrl("url1");
        //store
        experimentExecutionRepository.saveAndFlush(experimentExecution);
        log.debug("Execution Experiment : {}", experimentExecution.toString());
        //initialize again the object that has been modified by jpa
        experimentExecution.executionId(executionId)
                .state(ExperimentState.INIT)
                .testCaseDescriptorConfiguration(testCaseExecutionConfigurations)
                .testCaseResult(testCaseResults)
                .reportUrl("url1");
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        assertEquals(experimentExecution, experimentExecutionOptional.get());
        //delete
        experimentExecutionRepository.delete(experimentExecutionOptional.get());
        experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        assertFalse(experimentExecutionOptional.isPresent());
    }

    @Test
    @Ignore
    public void experimentExecutionTest() throws Exception {
        //create
        String executionId = eemService.createExperimentExecutionInstance();
        assertNotNull(executionId);
        //retrieve
        ExperimentExecution experimentExecution = eemService.getExperimentExecution(executionId);
        assertNotNull(experimentExecution);
        //delete
        eemService.removeExperimentExecutionRecord(executionId);
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        assertFalse(experimentExecutionOptional.isPresent());
    }

    @Test
    @Ignore
    public void experimentExecutionSubscriptionTest() throws Exception {
        String executionId = eemService.createExperimentExecutionInstance();
        assertNotNull(executionId);
        ExperimentExecutionSubscriptionRequest request = new ExperimentExecutionSubscriptionRequest();
        request.subscriptionType(SubscriptionType.STATE)
                .executionId(executionId)
                .callbackURI("test");
        //subscribe
        String subscriptionId = eemService.subscribe(request);
        assertNotNull(subscriptionId);
        //unsubscribe
        eemService.unsubscribe(subscriptionId);
        Optional<ExperimentExecutionSubscription> experimentExecutionSubscription = experimentExecutionSubscriptionRepository.findBySubscriptionId(subscriptionId);
        assertFalse(experimentExecutionSubscription.isPresent());
    }

    @Test
    @Ignore
    public void readFileFromFolder() throws URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        String jobname = "tst";
        URL resource = classLoader.getResource("job-template.xml");
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            File template = new File(resource.getFile());
            String line;
            String configXML = "";
            try (FileReader reader = new FileReader(template);
                 BufferedReader br = new BufferedReader(reader)) {

                while ((line = br.readLine()) != null) {
                    configXML = configXML.concat(line);
                }
                String robotFile = "*** Settings ***\nLibrary&#009;SSHLibrary\nLibrary&#009;String\nLibrary&#009;Collections\nLibrary&#009;BuiltIn\n*** Test Cases ***\nExecution Test Case\n&#009;&#009;Log&#009;Robot Execution Done $$var$$.delay";

                String lines[] = robotFile.split("\\r?\\n");

                String robotFileInConfig = "";
                for (int i = 0; i < lines.length; i++){
                    log.debug("echo -e" + lines[i] + " >> ${WORKSPACE}/executionFile.robot");
                    robotFileInConfig = robotFileInConfig.concat("echo \"" + lines[i] + "\" >> ${WORKSPACE}/executionFile.robot").concat("\n");
                }
                log.debug(robotFileInConfig);
                configXML = configXML.replace("__ROBOT_FILE__", robotFileInConfig);
                configXML = configXML.replace("_JOB__DESCRIPTION__","Job for experiment: " + jobname );
                configXML = configXML.replace("__EXECUTION_ID__", jobname);
                log.debug(configXML);
//                jenkinsServer.createFolder("PROVA4");
//                FolderJob folderJob = new FolderJob("PROVA4",  jenkinsUri+"/job/PROVA4/");

            } catch (FileNotFoundException e) {
                log.error(e.getMessage());
            } catch (IOException e1) {
                log.error(e1.getMessage());
            }
            JenkinsServer jenkinsServer = new JenkinsServer(new URI(jenkinsUri), jenkinsUsername, jenkinsPassword);
            try {
                jenkinsServer.createJob(jobname, configXML);
            } catch (IOException e1) {
                log.error(e1.getMessage());
            }
        }

    }



    @Test
    @Ignore
    public void disableJob() throws URISyntaxException, IOException, InterruptedException {
        //JenkinsClient jenkinsClient = JenkinsClient.builder().endPoint(jenkinsUri).credentials(jenkinsUsername+":"+jenkinsPassword).build();


        JenkinsServer jenkinsServer = new JenkinsServer(new URI(jenkinsUri), jenkinsUsername, jenkinsPassword);

        String name = "yourJobName";


        System.out.println(jenkinsServer.getJob(name).getLastBuild().Stop());
    }



}
