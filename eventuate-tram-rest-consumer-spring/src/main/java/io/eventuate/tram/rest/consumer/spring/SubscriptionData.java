package io.eventuate.tram.rest.consumer.spring;

public class SubscriptionData {
  private String channels;

  public String getChannels() {
    return channels;
  }

  public void setChannels(String channels) {
    this.channels = channels;
  }
}
