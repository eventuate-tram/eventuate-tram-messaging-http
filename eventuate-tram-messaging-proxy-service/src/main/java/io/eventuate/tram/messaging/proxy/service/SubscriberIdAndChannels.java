package io.eventuate.tram.messaging.proxy.service;

import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.Objects;
import java.util.Set;

public class SubscriberIdAndChannels {
  private String subscriberId;
  private Set<String> channels;

  public SubscriberIdAndChannels() {
  }

  public SubscriberIdAndChannels(String subscriberId, Set<String> channels) {
    this.subscriberId = subscriberId;
    this.channels = channels;
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

  @Override
  public int hashCode() {
    return Objects.hash(subscriberId, channels);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
}
