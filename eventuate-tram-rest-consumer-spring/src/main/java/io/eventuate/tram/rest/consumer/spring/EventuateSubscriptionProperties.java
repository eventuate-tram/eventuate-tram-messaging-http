package io.eventuate.tram.rest.consumer.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;

@ConfigurationProperties(prefix = "eventuate.subscription")
public class EventuateSubscriptionProperties {
  private Map<String, MessageSubscriptionData> message = Collections.emptyMap();
  private Map<String, EventSubscriptionData> event = Collections.emptyMap();

  public Map<String, MessageSubscriptionData> getMessage() {
    return message;
  }

  public void setMessage(Map<String, MessageSubscriptionData> message) {
    this.message = message;
  }

  public Map<String, EventSubscriptionData> getEvent() {
    return event;
  }

  public void setEvent(Map<String, EventSubscriptionData> event) {
    this.event = event;
  }
}
