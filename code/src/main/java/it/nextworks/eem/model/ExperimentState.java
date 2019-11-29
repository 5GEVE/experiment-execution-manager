package it.nextworks.eem.model;

import java.util.Objects;
import io.swagger.annotations.ApiModel;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Execution state of the experiment
 */
public enum ExperimentState {
  INIT("INIT"),
    CONFIGURING("CONFIGURING"),
    RUNNING("RUNNING"),
    RUNNING_STEP("RUNNING_STEP"),
    PAUSED("PAUSED"),
    VALIDATING("VALIDATING"),
    COMPLETED("COMPLETED"),
    ABORTING("ABORTING"),
    ABORTED("ABORTED"),
    FAILED("FAILED");

  private String value;

  ExperimentState(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ExperimentState fromValue(String text) {
    for (ExperimentState b : ExperimentState.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
