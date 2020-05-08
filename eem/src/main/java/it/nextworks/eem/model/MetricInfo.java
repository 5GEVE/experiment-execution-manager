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
}
