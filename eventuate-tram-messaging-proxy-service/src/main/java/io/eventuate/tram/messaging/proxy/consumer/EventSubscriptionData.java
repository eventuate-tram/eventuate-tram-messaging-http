package io.eventuate.tram.messaging.proxy.consumer;

public class EventSubscriptionData {
  private String aggregate;
  private String events;
  private String baseUrl;

  public String getAggregate() {
    return aggregate;
  }

  public void setAggregate(String aggregate) {
    this.aggregate = aggregate;
  }

  public String getEvents() {
    return events;
  }

  public void setEvents(String events) {
    this.events = events;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }
}
