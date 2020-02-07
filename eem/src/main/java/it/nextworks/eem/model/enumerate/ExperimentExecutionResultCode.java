package it.nextworks.eem.model.enumerate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Experiment run type
 */
public enum ExperimentExecutionResultCode {
    SUCCESSFUL("SUCCESSFUL"),
    FAILED("FAILED");

    private String value;

    ExperimentExecutionResultCode(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ExperimentExecutionResultCode fromValue(String text) {
        for (ExperimentExecutionResultCode b : ExperimentExecutionResultCode.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}