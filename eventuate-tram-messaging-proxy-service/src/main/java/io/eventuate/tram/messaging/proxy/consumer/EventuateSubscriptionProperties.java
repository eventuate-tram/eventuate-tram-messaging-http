package io.eventuate.tram.messaging.proxy.consumer;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;

@ConfigurationProperties(prefix = "eventuate.subscription")
public class EventuateSubscriptionProperties {
  private Map<String, CommandSubscriptionData> command = Collections.emptyMap();
  private Map<String, MessageSubscriptionData> message = Collections.emptyMap();
  private Map<String, EventSubscriptionData> event = Collections.emptyMap();

  public Map<String, CommandSubscriptionData> getCommand() {
    return command;
  }

  public void setCommand(Map<String, CommandSubscriptionData> command) {
    this.command = command;
  }

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
