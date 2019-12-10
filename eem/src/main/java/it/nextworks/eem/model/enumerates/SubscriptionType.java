package it.nextworks.eem.model.enumerates;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets subscriptionType
 */
public enum SubscriptionType {
  STATE("EXPERIMENT_EXECUTION_CHANGE_STATE");

  private String value;

  SubscriptionType(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static SubscriptionType fromValue(String text) {
    for (SubscriptionType b : SubscriptionType.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
