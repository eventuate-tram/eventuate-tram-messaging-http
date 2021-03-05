package io.eventuate.tram.rest.consumer.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "subscription")
public class EventuateSubscriptionProperties {
  private Map<String, SubscriptionData> message;

  public Map<String, SubscriptionData> getMessage() {
    return message;
  }

  public void setMessage(Map<String, SubscriptionData> message) {
    this.message = message;
  }
}
