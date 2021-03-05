package io.eventuate.tram.rest.consumer.spring;

public class SubscriptionData {
  private String subscriberId;
  private String channels;
  private String callbackSubscriptionId;

  public String getSubscriberId() {
    return subscriberId;
  }

  public void setSubscriberId(String subscriberId) {
    this.subscriberId = subscriberId;
  }

  public String getChannels() {
    return channels;
  }

  public void setChannels(String channels) {
    this.channels = channels;
  }

  public String getCallbackSubscriptionId() {
    return callbackSubscriptionId;
  }

  public void setCallbackSubscriptionId(String callbackSubscriptionId) {
    this.callbackSubscriptionId = callbackSubscriptionId;
  }
}
