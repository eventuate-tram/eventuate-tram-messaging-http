package io.eventuate.tram.messaging.proxy.consumer.customereventexample;

public class CustomerCreditReservedEvent extends CustomerEvent {
  public CustomerCreditReservedEvent() {
  }

  public CustomerCreditReservedEvent(String orderId) {
    super(orderId);
  }
}
