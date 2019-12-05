package it.nextworks.eem.engine;

import it.nextworks.eem.model.ExecutionResult;
import it.nextworks.eem.model.ExperimentExecution;
import it.nextworks.eem.model.ExperimentState;
import it.nextworks.eem.model.TestCaseExecutionConfiguration;
import it.nextworks.eem.repos.ExperimentExecutionRepository;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class EEMServiceTests {

    private static final Logger log = LoggerFactory.getLogger(EEMServiceTests.class);

    @Autowired
    EemService eemService;

    @Autowired
    ExperimentExecutionRepository experimentExecutionRepository;

    @Test
    public void createExperimentExecutionInstanceTest() throws Exception {
        String executionId = eemService.createExperimentExecutionInstance();
        assertNotNull(executionId);
        log.debug("Execution Experiment Id : {}", executionId);
    }

    @Test
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
        Optional<ExperimentExecution> experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        Assert.assertEquals(experimentExecution, experimentExecutionOptional.get());
        //delete
        experimentExecutionRepository.delete(experimentExecutionOptional.get());
        experimentExecutionOptional = experimentExecutionRepository.findByExecutionId(executionId);
        assertFalse(experimentExecutionOptional.isPresent());
    }
}
