package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import io.eventuate.util.test.async.Eventually;
import io.eventuate.util.test.async.EventuallyException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HttpMessagingProxyTest {

  @Value("${local.server.port}")
  private int port;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private TestMessageContainerBean testMessageContainerBean;

  @Autowired
  private MessageProducerImplementation messageProducerImplementation;

  private String subscriberId = "subscriber-" + generateId();
  private String channel = "channel-" + generateId();
  private String subscriptionId;

  @Test
  public void testSuscribe() {
    subscriberId = "subscriber-" + generateId();
    channel = "channel-" + generateId();

    String messageId = generateId();
    String payload = generateId();

    SubscribeRequest subscribeRequest = new SubscribeRequest(subscriberId, Collections.singleton(channel), callbackUrl());

    subscriptionId = restTemplate.postForObject(subscribeUrl(), subscribeRequest, String.class);

    Message message = sendMessage(payload, messageId);

    Eventually.eventually(() -> {
      Assert.assertEquals(1, testMessageContainerBean.getMessages().size());

      MessageResponse messageResponse = testMessageContainerBean.getMessages().get(0);

      Assert.assertEquals(messageId, messageResponse.getId());
      Assert.assertEquals(message.getHeaders(), messageResponse.getHeaders());
      Assert.assertEquals(payload, messageResponse.getPayload());
      Assert.assertEquals(subscriptionId, messageResponse.getSubscriptionId());
    });
  }


  @Test(expected = EventuallyException.class)
  public void testUnsuscribe() throws InterruptedException {
    testSuscribe();

    restTemplate.delete(unsubscribeUrl(subscriptionId));

    Thread.sleep(3000);

    sendMessage(generateId(), generateId());

    Eventually.eventually(() -> Assert.assertTrue(testMessageContainerBean.getMessages().size() > 1));
  }

  private Message sendMessage(String payload, String id) {
    Message message = MessageBuilder
            .withPayload(payload)
            .withHeader(Message.ID, id)
            .withHeader(Message.DESTINATION, channel)
            .build();

    messageProducerImplementation.send(message);

    return message;
  }


  private String unsubscribeUrl(String subscriptionId) {
    return host() + "/unsubscribe/" + subscriptionId;
  }

  private String subscribeUrl() {
    return host() + "/subscriptions";
  }

  private String callbackUrl() {
    return host() + "/messages";
  }

  private String host() {
    return String.format("http://localhost:%s", port);
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
