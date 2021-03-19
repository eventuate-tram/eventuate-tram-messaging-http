package io.eventuate.tram.messaging.proxy.consumer;

public class MessageSubscriptionData {
  private String channels;
  private String baseUrl;

  public String getChannels() {
    return channels;
  }

  public void setChannels(String channels) {
    this.channels = channels;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }
}
