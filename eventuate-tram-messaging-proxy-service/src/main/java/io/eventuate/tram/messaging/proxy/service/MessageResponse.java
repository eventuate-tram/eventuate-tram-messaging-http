package io.eventuate.tram.messaging.proxy.service;

import java.util.Map;

public class MessageResponse {
  private String id;
  private Map<String, String> headers;
  private String payload;
  private String subscriptionId;

  public MessageResponse() {
  }

  public MessageResponse(String id, Map<String, String> headers, String payload) {
    this.id = id;
    this.headers = headers;
    this.payload = payload;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public String getSubscriptionId() {
    return subscriptionId;
  }

  public void setSubscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
  }
}
