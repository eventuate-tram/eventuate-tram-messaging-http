package io.eventuate.tram.messaging.proxy.consumer;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class CommandSubscriptionData {
  private String channel;
  private String resource;
  private String baseUrl;
  private String commands;

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
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

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this);
  }
}
