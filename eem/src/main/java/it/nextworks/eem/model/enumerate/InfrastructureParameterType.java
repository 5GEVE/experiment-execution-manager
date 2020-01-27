package it.nextworks.eem.model.enumerate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Experiment run type
 */
public enum InfrastructureParameterType {
    SAP_IP_ADDRESS("SAP_IP_ADDRESS"),
    VNF_CP_IP_ADDRESS("VNF_CP_IP_ADDRESS"),
    VDU_CP_IP_ADDRESS("VDU_CP_IP_ADDRESS"),
    PNF_CP_IP_ADDRESS("PNF_CP_IP_ADDRESS");

    private String value;

    InfrastructureParameterType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static InfrastructureParameterType fromValue(String text) {
        for (InfrastructureParameterType b : InfrastructureParameterType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}