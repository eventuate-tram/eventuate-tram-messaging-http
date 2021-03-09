package io.eventuate.tram.rest.consumer.spring;

import io.eventuate.tram.consumer.http.common.HttpMessage;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import io.eventuate.tram.spring.events.publisher.TramEventsPublisherConfiguration;
import io.eventuate.tram.spring.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.junit.Assert;
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
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EventuateHttpMessageSubscriberTest.Config.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class EventuateHttpMessageSubscriberTest {
  @Configuration
  @Import({EventuateMessageSubscriberConfiguration.class, TramMessageProducerJdbcConfiguration.class, TramEventsPublisherConfiguration.class})
  @EnableAutoConfiguration
  @ComponentScan
  public static class Config {
  }

  @Autowired
  private MessageProducerImplementation messageProducerImplementation;

  @Autowired
  private DomainEventPublisher domainEventPublisher;

  @Autowired
  private TestController testController;

  private String channel = "test-channel";


  private String id;
  private String payload;

  @Before
  public void init() {
    payload = generateId();
  }

  @Test
  public void testMessageHandled() throws InterruptedException{
    sendMessage();
    assertMessage();
  }

  @Test
  public void testEventHandled() throws InterruptedException{
    sendEvent();
    assertEvent();
  }

  private void sendEvent() {
    id = generateId();
    domainEventPublisher.publish("TestAggregate", id, singletonList(new TestEvent(id, payload)));
  }

  private void assertEvent() throws InterruptedException {
    TestEvent testEvent = testController.getReceivedEvents().poll(30, TimeUnit.SECONDS);
    Assert.assertNotNull(testEvent);
    Assert.assertEquals(id, testEvent.getId());
    Assert.assertEquals(payload, testEvent.getSomeImportantData());
  }

  private void sendMessage() {
    Message message = MessageBuilder
            .withPayload(payload)
            .withHeader(Message.DESTINATION, channel)
            .build();

    messageProducerImplementation.send(message);

    id = message.getId();
  }

  private void assertMessage() throws InterruptedException {
    HttpMessage message = testController.getReceivedMessages().poll(30, TimeUnit.SECONDS);
    Assert.assertNotNull(message);
    Assert.assertEquals(id, message.getId());
    Assert.assertEquals(payload, message.getPayload());
    Assert.assertEquals(channel, message.getHeaders().get(Message.DESTINATION));
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
