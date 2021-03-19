package io.eventuate.tram.messaging.proxy.consumer.customereventexample;

public class CustomerValidationFailedEvent extends CustomerEvent {
  public CustomerValidationFailedEvent() {
  }

  public CustomerValidationFailedEvent(String orderId) {
    super(orderId);
  }
}
