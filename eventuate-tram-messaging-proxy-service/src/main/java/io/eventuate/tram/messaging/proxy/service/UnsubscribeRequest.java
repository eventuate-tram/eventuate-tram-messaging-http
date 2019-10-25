package io.eventuate.tram.messaging.proxy.service;

import java.util.Set;

public class UnsubscribeRequest {
  private String subscriberId;
  private Set<String> channels;

  public UnsubscribeRequest() {
  }

  public UnsubscribeRequest(String subscriberId, Set<String> channels) {
    this.subscriberId = subscriberId;
    this.channels = channels;
  }

  public String getSubscriberId() {
    return subscriberId;
  }

  public void setSubscriberId(String subscriberId) {
    this.subscriberId = subscriberId;
  }

  public Set<String> getChannels() {
    return channels;
  }

  public void setChannels(Set<String> channels) {
    this.channels = channels;
  }
}
