package io.eventuate.tram.messaging.proxy.consumer.customereventexample;

import io.eventuate.tram.events.common.DomainEvent;

public class CustomerEvent implements DomainEvent {
  private String orderId;

  public CustomerEvent() {
  }

  public CustomerEvent(String orderId) {
    this.orderId = orderId;
  }

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }
}
