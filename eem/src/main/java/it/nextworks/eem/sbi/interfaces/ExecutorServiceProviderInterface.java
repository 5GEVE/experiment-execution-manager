package it.nextworks.eem.sbi.interfaces;

public interface ExecutorServiceProviderInterface {

    void runTestCase(String executionId, String tcDescriptorId, String executionScript);
    void abortTestCase(String executionId, String tcDescriptorId);
}
