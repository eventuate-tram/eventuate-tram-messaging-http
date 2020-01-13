package io.eventuate.tram.messaging.proxy.service;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Objects;
import java.util.Set;

public class SubscriptionInfo {
  private String subscriptionInstanceId;
  private String subscriberId;
  private Set<String> channels;
  private String callbackUrl;
  private boolean follower;

  public SubscriptionInfo() {
  }

  public SubscriptionInfo(String subscriptionInstanceId, String subscriberId, Set<String> channels, String callbackUrl, boolean follower) {
    this.subscriptionInstanceId = subscriptionInstanceId;
    this.subscriberId = subscriberId;
    this.channels = channels;
    this.callbackUrl = callbackUrl;
    this.follower = follower;
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

  public boolean isFollower() {
    return follower;
  }

  public void setFollower(boolean follower) {
    this.follower = follower;
  }

  @Override
  public boolean equals(Object o) {
    return EqualsBuilder.reflectionEquals(this, o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subscriptionInstanceId, subscriberId, channels, callbackUrl);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
