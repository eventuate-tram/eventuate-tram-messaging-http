package io.eventuate.tram.messaging.proxy.consumer;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

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

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this);
  }
}
