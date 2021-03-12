package io.eventuate.tram.messaging.proxy.consumer;

import io.eventuate.tram.commands.common.CommandReplyOutcome;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.consumer.http.common.HttpMessage;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import io.eventuate.tram.messaging.proxy.service.ProxyConfiguration;
import io.eventuate.tram.messaging.proxy.service.SubscriptionController;
import io.eventuate.tram.spring.commands.producer.TramCommandProducerConfiguration;
import io.eventuate.tram.spring.events.publisher.TramEventsPublisherConfiguration;
import io.eventuate.tram.spring.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EventuateHttpMessageSubscriberTest.Config.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class EventuateHttpMessageSubscriberTest {
  @Configuration
  @Import({ProxyConfiguration.class,
          EventuateMessageSubscriberConfiguration.class,
          TramMessageProducerJdbcConfiguration.class,
          TramEventsPublisherConfiguration.class,
          TramCommandProducerConfiguration.class})
  @EnableAutoConfiguration
  @ComponentScan
  public static class Config {
    @Bean
    public SubscriptionController subscriptionController() {
      return new SubscriptionController();
    }
  }

  @Autowired
  private MessageProducerImplementation messageProducerImplementation;

  @Autowired
  private DomainEventPublisher domainEventPublisher;

  @Autowired
  private TestController testController;

  @Autowired
  private CommandProducer commandProducer;

  private String messageChannel = "test-channel";
  private String commandChannel = "test-command-channel";
  private String commandReplyChannel = "test-reply-channel";
  private String commandResource;

  private String commandId;

  private String id;
  private String payload;

  @Before
  public void init() {
    payload = generateId();
    commandResource = generateId();
  }

  @Test
  public void testMessageHandled() throws InterruptedException {
    sendMessage();
    assertMessage();
  }

  @Test
  public void testEventHandled() throws InterruptedException {
    sendEvent();
    assertEvent();
  }

  @Test
  public void testCommandHandled() throws InterruptedException {
    sendCommand();
    assertCommand();
    assertReply();
  }

  private void sendCommand() {
    id = commandProducer.send(commandChannel,
            String.format("/test-resource/%s", commandResource),
            new TestCommand(payload),
            commandReplyChannel,
            Collections.emptyMap());
  }

  private void assertCommand() throws InterruptedException {
    TestCommandInfo testCommandInfo = testController.getReceivedCommands().poll(30, TimeUnit.SECONDS);
    assertNotNull(testCommandInfo);
    assertNotNull(testCommandInfo.getMessageId());
    commandId = testCommandInfo.getMessageId();
    assertEquals(payload, testCommandInfo.getTestCommand().getSomeImportantData());
    assertEquals(commandReplyChannel, testCommandInfo.getReplyChannel());
    assertEquals(commandResource, testCommandInfo.getValue());

    Map<String, String> headers = testCommandInfo.getHeaders();

    assertEquals("/test-resource/" + commandResource, headers.get("commandreply_resource"));
    assertEquals(commandChannel, headers.get("commandreply__destination"));
    assertEquals(commandReplyChannel, headers.get("commandreply_reply_to"));
    assertEquals(TestCommand.class.getName(), headers.get("commandreply_type"));
    assertEquals(id, headers.get("reply_to_message_id"));
  }

  private void assertReply() throws InterruptedException {
    TestReplyInfo testReplyInfo = testController.getReceivedReplies().poll(30, TimeUnit.SECONDS);

    assertNotNull(testReplyInfo);
    assertEquals(CommandReplyOutcome.SUCCESS, testReplyInfo.getOutcome());
    assertEquals(String.format("reply to %s", commandId), testReplyInfo.getReply().getSomeImportantData());
    assertEquals(commandId, testReplyInfo.getReplyToCommandId());
    assertEquals(TestReply.class.getName(), testReplyInfo.getReplyType());
    assertEquals(commandResource, testReplyInfo.getResourceValue());
  }

  private void sendEvent() {
    id = generateId();
    domainEventPublisher.publish("TestAggregate", id, singletonList(new TestEvent(payload)));
  }

  private void assertEvent() throws InterruptedException {
    TestEventInfo testEventInfo = testController.getReceivedEvents().poll(30, TimeUnit.SECONDS);
    assertNotNull(testEventInfo);
    assertNotNull(testEventInfo.getEventId());
    assertEquals(payload, testEventInfo.getTestEvent().getSomeImportantData());
    assertEquals(id, testEventInfo.getAggregateId());
  }

  private void sendMessage() {
    Message message = MessageBuilder
            .withPayload(payload)
            .withHeader(Message.DESTINATION, messageChannel)
            .build();

    messageProducerImplementation.send(message);

    id = message.getId();
  }

  private void assertMessage() throws InterruptedException {
    HttpMessage message = testController.getReceivedMessages().poll(30, TimeUnit.SECONDS);
    assertNotNull(message);
    assertEquals(id, message.getId());
    assertEquals(payload, message.getPayload());
    assertEquals(messageChannel, message.getHeaders().get(Message.DESTINATION));
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
