package it.nextworks.eem.sbi.interfaces;

public interface ExecutorServiceProviderInterface {

    void runTestCase(String executionId, String tcDescriptorId, String testCaseFile);
    void abortTestCase(String executionId, String tcDescriptorId);
}
