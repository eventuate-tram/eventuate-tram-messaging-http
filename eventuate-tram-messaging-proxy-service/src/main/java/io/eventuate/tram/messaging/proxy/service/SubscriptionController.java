package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.http.common.HttpMessage;
import io.eventuate.tram.consumer.http.common.SubscribeRequest;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
public class SubscriptionController {

  @Autowired
  private ProxyProperties proxyProperties;

  @Autowired
  private MessageConsumerImplementation messageConsumerImplementation;

  @Autowired
  private RestTemplate restTemplate;

  private ConcurrentMap<String, MessageSubscription> messageSubscriptions = new ConcurrentHashMap<>();
  private ConcurrentMap<String, Long> messageSubscriptionUpdateTime = new ConcurrentHashMap<>();

  @RequestMapping(value = "/subscriptions", method = RequestMethod.POST)
  public String subscribe(@RequestBody SubscribeRequest subscribeRequest) {
    String subscriptionInstanceId = generateId();

    messageSubscriptionUpdateTime.put(subscriptionInstanceId, System.currentTimeMillis());

    MessageSubscription messageSubscription = messageConsumerImplementation.subscribe(subscribeRequest.getSubscriberId(),
            subscribeRequest.getChannels(),
            message -> {

              if (System.currentTimeMillis() - messageSubscriptionUpdateTime.get(subscriptionInstanceId) > proxyProperties.getMaxHeartbeatInterval()) {
                unsubscribe(subscriptionInstanceId);
                throw new RuntimeException("Heartbeat timeout.");
              }

              restTemplate.postForLocation(subscribeRequest.getCallbackUrl() + "/" + subscriptionInstanceId,
                      new HttpMessage(message.getId(), message.getHeaders(), message.getPayload()));
            });

    messageSubscriptions.put(subscriptionInstanceId, messageSubscription);

    return subscriptionInstanceId;
  }

  @RequestMapping(value = "/subscriptions/{subscriptionInstanceId}/heartbeat", method = RequestMethod.POST)
  public void heartbeat(@PathVariable(name = "subscriptionInstanceId") String subscriptionInstanceId) {
    messageSubscriptionUpdateTime.put(subscriptionInstanceId, System.currentTimeMillis());
  }

  @RequestMapping(value = "/subscriptions/{subscriptionInstanceId}", method = RequestMethod.DELETE)
  public void unsubscribe(@PathVariable(name = "subscriptionInstanceId") String subscriptionInstanceId) {
    Optional
            .ofNullable(messageSubscriptions.remove(subscriptionInstanceId))
            .ifPresent(MessageSubscription::unsubscribe);

    messageSubscriptionUpdateTime.remove(subscriptionInstanceId);
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }

}
