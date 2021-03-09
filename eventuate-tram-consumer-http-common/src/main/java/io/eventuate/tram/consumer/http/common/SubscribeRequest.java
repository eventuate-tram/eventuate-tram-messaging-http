package io.eventuate.tram.consumer.http.common;

import io.micronaut.core.annotation.Introspected;

import java.util.Set;

@Introspected
public class SubscribeRequest {
  private SubscriptionType subscriptionType;
  private String subscriberId;
  private Set<String> channels;
  private String callbackUrl;
  private boolean discardSubscriptionIdInCallbackUrl;

  public SubscribeRequest() {
  }

  public SubscribeRequest(SubscriptionType subscriptionType,
                          String subscriberId,
                          Set<String> channels,
                          String callbackUrl,
                          boolean discardSubscriptionIdInCallbackUrl) {

    this.subscriptionType = subscriptionType;
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
}
