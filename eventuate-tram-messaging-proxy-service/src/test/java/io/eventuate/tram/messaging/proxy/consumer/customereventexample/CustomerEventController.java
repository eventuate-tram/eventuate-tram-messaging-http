package io.eventuate.tram.messaging.proxy.consumer.customereventexample;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ConcurrentHashMap;

@RestController
public class CustomerEventController {
  private ConcurrentHashMap<String, CustomerEvent> receivedEvents = new ConcurrentHashMap<>();

  public ConcurrentHashMap<String, CustomerEvent> getReceivedEvents() {
    return receivedEvents;
  }

  @PostMapping(path = "/events/s5/Customer/{aggregateId}/io.eventuate.tram.messaging.proxy.consumer.customereventexample.CustomerCreditReservedEvent/{eventId}")
  public void handleCustomerCreditReservedEvent(@PathVariable String aggregateId, @PathVariable String eventId, @RequestBody CustomerCreditReservedEvent customerCreditReservedEvent) {
    receivedEvents.put("handleCustomerCreditReservedEvent", customerCreditReservedEvent);
  }

  @PostMapping(path = "/events/s5/Customer/{aggregateId}/io.eventuate.tram.messaging.proxy.consumer.customereventexample.CustomerCreditReservationFailedEvent/{eventId}")
  public void handleCustomerCreditReservationFailedEvent(@PathVariable String aggregateId, @PathVariable String eventId, @RequestBody CustomerCreditReservationFailedEvent customerCreditReservationFailedEvent) {
    receivedEvents.put("handleCustomerCreditReservationFailedEvent", customerCreditReservationFailedEvent);
  }

  @PostMapping(path = "/events/s5/Customer/{aggregateId}/io.eventuate.tram.messaging.proxy.consumer.customereventexample.CustomerValidationFailedEvent/{eventId}")
  public void handleCustomerValidationFailedEvent(@PathVariable String aggregateId, @PathVariable String eventId, @RequestBody CustomerValidationFailedEvent customerValidationFailedEvent) {
    receivedEvents.put("handleCustomerValidationFailedEvent", customerValidationFailedEvent);
  }
}
