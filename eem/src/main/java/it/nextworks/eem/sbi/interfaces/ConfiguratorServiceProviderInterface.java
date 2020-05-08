package it.nextworks.eem.sbi.interfaces;
import it.nextworks.eem.model.MetricInfo;

import java.util.List;

public interface ConfiguratorServiceProviderInterface {

    void applyConfiguration(String executionId, String tcDescriptorId, String configScript);

    void abortConfiguration(String executionId, String tcDescriptorId);

    void configureInfrastructureMetricCollection(String executionId, String tcDescriptorId, List<MetricInfo> metrics);

    void resetConfiguration(String executionId, String tcDescriptorId, String resetScript);

    void removeInfrastructureMetricCollection(String executionId, String tcDescriptorId, List<String> metricConfigIds);

}
