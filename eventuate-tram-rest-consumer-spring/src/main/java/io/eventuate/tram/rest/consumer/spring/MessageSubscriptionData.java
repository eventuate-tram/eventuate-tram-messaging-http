package io.eventuate.tram.rest.consumer.spring;

public class MessageSubscriptionData {
  private String channels;
  private String url;

  public String getChannels() {
    return channels;
  }

  public void setChannels(String channels) {
    this.channels = channels;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
