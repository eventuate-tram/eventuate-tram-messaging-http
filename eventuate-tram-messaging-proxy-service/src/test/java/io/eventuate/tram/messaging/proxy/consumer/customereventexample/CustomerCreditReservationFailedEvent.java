package io.eventuate.tram.messaging.proxy.consumer.customereventexample;

public class CustomerCreditReservationFailedEvent extends CustomerEvent {
  public CustomerCreditReservationFailedEvent() {
  }

  public CustomerCreditReservationFailedEvent(String orderId) {
    super(orderId);
  }
}
