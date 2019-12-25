package io.eventuate.tram.messaging.proxy.service;

import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.Objects;
import java.util.Set;

public class SubscriptionInfo {
  private String subscriptionInstanceId;
  private String subscriberId;
  private Set<String> channels;
  private String callbackUrl;

  public SubscriptionInfo() {
  }

  public SubscriptionInfo(String subscriptionInstanceId, String subscriberId, Set<String> channels, String callbackUrl) {
    this.subscriptionInstanceId = subscriptionInstanceId;
    this.subscriberId = subscriberId;
    this.channels = channels;
    this.callbackUrl = callbackUrl;
  }

  public String getSubscriptionInstanceId() {
    return subscriptionInstanceId;
  }

  public void setSubscriptionInstanceId(String subscriptionInstanceId) {
    this.subscriptionInstanceId = subscriptionInstanceId;
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

  @Override
  public boolean equals(Object o) {
    return EqualsBuilder.reflectionEquals(this, o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subscriptionInstanceId, subscriberId, channels, callbackUrl);
  }
}
