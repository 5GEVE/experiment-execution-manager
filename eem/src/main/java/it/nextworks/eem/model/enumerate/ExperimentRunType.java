package it.nextworks.eem.model.enumerate;

import com.fasterxml.jackson.annotation.JsonValue;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Experiment run type
 */
public enum ExperimentRunType {
    RUN_ALL("RUN_ALL"),
    RUN_IN_STEPS("RUN_IN_STEPS");

    private String value;

    ExperimentRunType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ExperimentRunType fromValue(String text) {
        for (ExperimentRunType b : ExperimentRunType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}