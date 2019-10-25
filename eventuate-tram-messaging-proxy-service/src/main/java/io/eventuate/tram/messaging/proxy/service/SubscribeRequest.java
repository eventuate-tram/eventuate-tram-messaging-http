package io.eventuate.tram.messaging.proxy.service;

import java.util.Set;

public class SubscribeRequest {
  private String subscriberId;
  private Set<String> channels;
  private String callbackUrl;

  public SubscribeRequest() {
  }

  public SubscribeRequest(String subscriberId, Set<String> channels, String callbackUrl) {
    this.subscriberId = subscriberId;
    this.channels = channels;
    this.callbackUrl = callbackUrl;
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

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }
}
