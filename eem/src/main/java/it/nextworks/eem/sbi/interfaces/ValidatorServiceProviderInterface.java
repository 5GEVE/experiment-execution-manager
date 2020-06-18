package it.nextworks.eem.sbi.interfaces;

public interface ValidatorServiceProviderInterface {

    void configureExperiment(String experimentId, String executionId);
    void startTcValidation(String experimentId, String executionId, String tcDescriptorId);
    void stopTcValidation(String experimentId, String executionId, String tcDescriptorId);
    void queryValidationResult(String experimentId, String executionId, String tcDescriptorId);
    void terminateExperiment(String experimentId, String executionId);
}
