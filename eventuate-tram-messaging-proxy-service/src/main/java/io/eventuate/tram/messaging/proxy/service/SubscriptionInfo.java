package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.tram.consumer.http.common.SubscriptionType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Objects;
import java.util.Set;

public class SubscriptionInfo {
  private SubscriptionType subscriptionType;
  private String subscriptionInstanceId;
  private String subscriberId;
  private Set<String> channels;
  private String callbackUrl;
  private boolean discardSubscriptionIdInCallbackUrl;

  public SubscriptionInfo() {
  }

  public SubscriptionInfo(SubscriptionType subscriptionType,
                          String subscriptionInstanceId,
                          String subscriberId,
                          Set<String> channels,
                          String callbackUrl,
                          boolean discardSubscriptionIdInCallbackUrl) {
    this.subscriptionType = subscriptionType;
    this.subscriptionInstanceId = subscriptionInstanceId;
    this.subscriberId = subscriberId;
    this.channels = channels;
    this.callbackUrl = callbackUrl;
    this.discardSubscriptionIdInCallbackUrl = discardSubscriptionIdInCallbackUrl;
  }

  public SubscriptionType getSubscriptionType() {
    return subscriptionType;
  }

  public void setSubscriptionType(SubscriptionType subscriptionType) {
    this.subscriptionType = subscriptionType;
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

  public boolean isDiscardSubscriptionIdInCallbackUrl() {
    return discardSubscriptionIdInCallbackUrl;
  }

  public void setDiscardSubscriptionIdInCallbackUrl(boolean discardSubscriptionIdInCallbackUrl) {
    this.discardSubscriptionIdInCallbackUrl = discardSubscriptionIdInCallbackUrl;
  }

  @Override
  public boolean equals(Object o) {
    return EqualsBuilder.reflectionEquals(this, o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subscriptionType, subscriptionInstanceId, subscriberId, channels, callbackUrl, discardSubscriptionIdInCallbackUrl);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
