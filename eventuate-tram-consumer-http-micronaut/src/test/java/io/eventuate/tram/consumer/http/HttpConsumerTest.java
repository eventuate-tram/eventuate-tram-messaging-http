package io.eventuate.tram.consumer.http;

import io.eventuate.common.jdbc.EventuateTransactionTemplate;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import io.micronaut.context.annotation.Value;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@MicronautTest(transactional = false)
public class HttpConsumerTest {

  @Inject
  private MessageConsumerImplementation eventuateTramHttpMessageConsumer;

  @Inject
  private MessageProducerImplementation messageProducerImplementation;

  @Inject
  private EventuateTramHttpMessageController eventuateTramHttpMessageController;

  @Inject
  private EventuateTransactionTemplate eventuateTransactionTemplate;

  @Inject
  private ProxyClient proxyClient;

  @Inject
  private HeartbeatService heartbeatService;

  @Value("${eventuate.http.proxy.base.url}")
  private String httpProxyBaseUrl;

  @Value("${eventuate.http.consumer.base.url}")
  private String httpConsumerBaseUrl;

  @Value("${micronaut.server.port}")
  private String micronautServerPort;

  private String subscriberId;
  private String id;
  private String payload;
  private String channel;

  private BlockingQueue<Message> messages;

  @Test
  public void testSuscribe() throws InterruptedException {
    subscriberId = "subscriber-" + generateId();
    id = "id-" + generateId();
    payload = "payload-" + generateId();
    channel = "channel-" + generateId();

    messages = new LinkedBlockingQueue<>();

    eventuateTramHttpMessageConsumer.subscribe(subscriberId, Collections.singleton(channel), messages::add);

    sendMessage();

    Message message = messages.poll(10, TimeUnit.SECONDS);

    Assert.assertNotNull(message);
    Assert.assertEquals(id, message.getId());
    Assert.assertEquals(payload, message.getPayload());
    Assert.assertEquals(channel, message.getRequiredHeader(Message.DESTINATION));
  }

  @Test
  public void testUnSuscribe() throws InterruptedException {
    testSuscribe();

    eventuateTramHttpMessageConsumer.close();

    Thread.sleep(3000);

    id = "id-" + generateId();
    payload = "payload-" + generateId();

    sendMessage();

    Message message = messages.poll(10, TimeUnit.SECONDS);

    Assert.assertNull(message);
  }

  @Test
  public void testMessageProcessedByNewConsumerWhenPreviousProcessingFailed() throws InterruptedException {
    subscriberId = "subscriber-" + generateId();
    id = "id-" + generateId();
    payload = "payload-" + generateId();
    channel = "channel-" + generateId();
    messages = new LinkedBlockingQueue<>();

    EventuateTramHttpMessageConsumer eventuateTramHttpMessageConsumer =
            new EventuateTramHttpMessageConsumer(proxyClient,
                    heartbeatService,
                    eventuateTramHttpMessageController,
                    "http://localhost:" + micronautServerPort + "/someNonExistentAddress");

    eventuateTramHttpMessageConsumer.subscribe(subscriberId, Collections.singleton(channel), messages::add);

    sendMessage();

    Message message = messages.poll(10, TimeUnit.SECONDS);

    Assert.assertNull(message);

    this.eventuateTramHttpMessageConsumer.subscribe(subscriberId, Collections.singleton(channel), messages::add);

    message = messages.poll(10, TimeUnit.SECONDS);

    Assert.assertNotNull(message);
  }

  private void sendMessage() {
    Message message = MessageBuilder
            .withPayload(payload)
            .withHeader(Message.ID, id)
            .withHeader(Message.DESTINATION, channel)
            .build();

    messageProducerImplementation.send(message);
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
