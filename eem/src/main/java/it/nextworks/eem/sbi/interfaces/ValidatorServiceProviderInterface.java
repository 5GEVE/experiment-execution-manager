package it.nextworks.eem.sbi.interfaces;

public interface ValidatorServiceProviderInterface {

    void configureExperiment(String executionId);
    void startTcValidation(String executionId, String tcDescriptorId);
    void stopTcValidation(String executionId, String tcDescriptorId);
    void queryValidationResult(String executionId, String tcDescriptorId);
    void terminateExperiment(String executionId);
}
