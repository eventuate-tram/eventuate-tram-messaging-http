package io.eventuate.tram.consumer.http;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import io.eventuate.util.test.async.Eventually;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micronaut.context.annotation.Value;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
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
  private ProxyClient proxyClient;

  @Inject
  private HeartbeatService heartbeatService;

  @Inject
  private CircuitBreaker circuitBreaker;

  @Inject
  private Retry retry;

  @Value("${micronaut.server.port}")
  private String micronautServerPort;

  @Value("${eventuate.http.proxy.base.url}")
  private String proxyUrl;

  private String subscriberId;
  private String id;
  private String payload;
  private String channel;

  private BlockingQueue<Message> messages;

  @BeforeEach
  public void init() {
    subscriberId = "subscriber-" + generateId();
    id = "id-" + generateId();
    payload = "\"payload-" + generateId() + "\"";
    channel = "channel-" + generateId();
    messages = new LinkedBlockingQueue<>();
  }

  @Test
  public void testSubscribe() throws InterruptedException {
    eventuateTramHttpMessageConsumer.subscribe(subscriberId, Collections.singleton(channel), messages::add);
    sendMessage();
    assertMessage();
  }

  @Test
  public void testPersistence() throws InterruptedException, IOException {
    eventuateTramHttpMessageConsumer.subscribe(subscriberId, Collections.singleton(channel), messages::add);

    Assert.assertTrue(isProxyAvailable());
    executeScript("stop-proxies.sh");
    Eventually.eventually(() -> Assert.assertFalse(isProxyAvailable()));
    executeScript("start-proxy.sh");
    Eventually.eventually(() -> Assert.assertTrue(isProxyAvailable()));

    sendMessage();
    assertMessage();
  }

  @Test
  public void testFollowingSubscription() throws InterruptedException, IOException {
    eventuateTramHttpMessageConsumer.subscribe(subscriberId, Collections.singleton(channel), messages::add);

    Assert.assertTrue(isProxyAvailable());
    executeScript("stop-proxy.sh");
    Eventually.eventually(() -> Assert.assertFalse(isProxyAvailable()));

    sendMessage();
    assertMessage();
    executeScript("start-proxy.sh");
    Eventually.eventually(() -> Assert.assertTrue(isProxyAvailable()));
  }

  @Test
  public void testUnsubscribe() throws InterruptedException {
    testSubscribe();

    eventuateTramHttpMessageConsumer.close();

    Thread.sleep(3000);

    id = "id-" + generateId();
    payload = "\"payload-" + generateId() + "\"";

    sendMessage();

    Message message = messages.poll(30, TimeUnit.SECONDS);

    Assert.assertNull(message);
  }

  @Test
  public void testMessageProcessedByNewConsumerWhenPreviousProcessingFailed() throws InterruptedException {
    EventuateTramHttpMessageConsumer eventuateTramHttpMessageConsumer =
            new EventuateTramHttpMessageConsumer(circuitBreaker,
                    retry,
                    proxyClient,
                    heartbeatService,
                    eventuateTramHttpMessageController,
                    "http://localhost:" + micronautServerPort + "/someNonExistentAddress");

    eventuateTramHttpMessageConsumer.subscribe(subscriberId, Collections.singleton(channel), messages::add);

    sendMessage();

    Message message = messages.poll(30, TimeUnit.SECONDS);

    Assert.assertNull(message);

    this.eventuateTramHttpMessageConsumer.subscribe(subscriberId, Collections.singleton(channel), messages::add);

    assertMessage();
  }

  private void assertMessage() throws InterruptedException {
    Message message = messages.poll(30, TimeUnit.SECONDS);
    Assert.assertNotNull(message);
    Assert.assertEquals(id, message.getId());
    Assert.assertEquals(payload, message.getPayload());
    Assert.assertEquals(channel, message.getRequiredHeader(Message.DESTINATION));
  }

  private void executeScript(String script) throws IOException, InterruptedException {
    ProcessBuilder processBuilder = new ProcessBuilder();
    processBuilder.directory(new File(".."));
    processBuilder.command("sh", script);
    processBuilder.inheritIO();
    processBuilder.start().waitFor();
  }

  private boolean isProxyAvailable() {
    try (Socket socket = new Socket()) {
      URL url = new URL(proxyUrl);
      socket.connect(new InetSocketAddress(url.getHost(), url.getPort()), 3000);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  private void sendMessage() {
    Message message = MessageBuilder
            .withPayload(payload)
            .withHeader(Message.DESTINATION, channel)
            .build();

    messageProducerImplementation.send(message);

    id = message.getId();
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
