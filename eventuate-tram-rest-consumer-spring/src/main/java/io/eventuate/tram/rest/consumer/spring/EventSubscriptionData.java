package io.eventuate.tram.rest.consumer.spring;

public class EventSubscriptionData {
  private String aggregates;
  private String url;

  public String getAggregates() {
    return aggregates;
  }

  public void setAggregates(String aggregates) {
    this.aggregates = aggregates;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
