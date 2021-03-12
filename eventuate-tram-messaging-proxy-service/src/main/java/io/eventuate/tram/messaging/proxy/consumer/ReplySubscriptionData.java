package io.eventuate.tram.messaging.proxy.consumer;

public class ReplySubscriptionData {
  private String replyChannel;
  private String resource;
  private String baseUrl;
  private String commands;

  public String getReplyChannel() {
    return replyChannel;
  }

  public void setReplyChannel(String replyChannel) {
    this.replyChannel = replyChannel;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getCommands() {
    return commands;
  }

  public void setCommands(String commands) {
    this.commands = commands;
  }
}
