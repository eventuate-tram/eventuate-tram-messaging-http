package io.eventuate.tram.messaging.proxy.consumer.customereventexample;

import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.messaging.proxy.consumer.EventuateMessageSubscriberConfiguration;
import io.eventuate.tram.spring.events.publisher.TramEventsPublisherConfiguration;
import io.eventuate.tram.spring.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import io.eventuate.util.test.async.Eventually;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CustomerEventSubscriberTest.Config.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CustomerEventSubscriberTest {
  @Configuration
  @Import({EventuateMessageSubscriberConfiguration.class, TramMessageProducerJdbcConfiguration.class, TramEventsPublisherConfiguration.class})
  @EnableAutoConfiguration
  @ComponentScan
  public static class Config {
  }

  @Autowired
  private DomainEventPublisher domainEventPublisher;

  @Autowired
  private CustomerEventController customerEventController;

  private String customerCreditReservedOrderId;
  private String customerCreditReservationFailedOrderId;
  private String customerValidationFailedOrderId;


  @Before
  public void init() {
    customerCreditReservedOrderId = generateId();
    customerCreditReservationFailedOrderId = generateId();
    customerValidationFailedOrderId = generateId();
  }

  @Test
  public void testEventHandled() {
    sendEvents();
    assertEvents();
  }

  private void sendEvents() {
    domainEventPublisher.publish("Customer",
            customerCreditReservedOrderId, singletonList(new CustomerCreditReservedEvent(customerCreditReservedOrderId)));

    domainEventPublisher.publish("Customer",
            customerCreditReservationFailedOrderId, singletonList(new CustomerCreditReservationFailedEvent(customerCreditReservationFailedOrderId)));

    domainEventPublisher.publish("Customer",
            customerValidationFailedOrderId, singletonList(new CustomerValidationFailedEvent(customerValidationFailedOrderId)));
  }

  private void assertEvents() {
    Eventually.eventually(() -> {
      assertEquals(3, customerEventController.getReceivedEvents().size());

      CustomerCreditReservedEvent customerCreditReservedEvent =
              (CustomerCreditReservedEvent)customerEventController.getReceivedEvents().get("handleCustomerCreditReservedEvent");
      assertEquals(customerCreditReservedOrderId, customerCreditReservedEvent.getOrderId());

      CustomerCreditReservationFailedEvent customerCreditReservationFailedEvent =
              (CustomerCreditReservationFailedEvent)customerEventController.getReceivedEvents().get("handleCustomerCreditReservationFailedEvent");
      assertEquals(customerCreditReservationFailedOrderId, customerCreditReservationFailedEvent.getOrderId());

      CustomerValidationFailedEvent customerValidationFailedEvent =
              (CustomerValidationFailedEvent)customerEventController.getReceivedEvents().get("handleCustomerValidationFailedEvent");
      assertEquals(customerValidationFailedOrderId, customerValidationFailedEvent.getOrderId());
    });
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
