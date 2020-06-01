package it.nextworks.eem.model;

import it.nextworks.nfvmano.catalogue.blueprint.elements.EveSite;
import it.nextworks.nfvmano.catalogue.blueprint.elements.InfrastructureMetric;

public class MetricInfo {

    private InfrastructureMetric metric;
    private String topic;
    private EveSite targetSite;

    public MetricInfo(InfrastructureMetric metric, String topic, EveSite targetSite) {
        this.metric = metric;
        this.topic = topic;
        this.targetSite = targetSite;
    }

    public InfrastructureMetric getMetric() {
        return metric;
    }

    public void setMetric(InfrastructureMetric metric) {
        this.metric = metric;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public EveSite getTargetSite() {
        return targetSite;
    }

    public void setTargetSite(EveSite targetSite) {
        this.targetSite = targetSite;
    }
}
