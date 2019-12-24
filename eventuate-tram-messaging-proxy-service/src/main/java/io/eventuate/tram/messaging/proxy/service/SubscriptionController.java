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

  private ConcurrentMap<String, SubscriptionUpdateTime> messageSubscriptions = new ConcurrentHashMap<>();

  @RequestMapping(value = "/subscriptions", method = RequestMethod.POST)
  public String subscribe(@RequestBody SubscribeRequest subscribeRequest) {
    String subscriptionInstanceId = generateId();

    MessageSubscription messageSubscription = messageConsumerImplementation.subscribe(subscribeRequest.getSubscriberId(),
            subscribeRequest.getChannels(),
            message -> {

              long lastUpdateTime = Optional
                      .ofNullable(messageSubscriptions.get(subscriptionInstanceId))
                      .map(SubscriptionUpdateTime::getUpdateTime)
                      .orElse(System.currentTimeMillis());

              if (System.currentTimeMillis() - lastUpdateTime > proxyProperties.getMaxHeartbeatInterval()) {
                unsubscribe(subscriptionInstanceId);
                throw new RuntimeException("Heartbeat timeout.");
              }

              restTemplate.postForLocation(subscribeRequest.getCallbackUrl() + "/" + subscriptionInstanceId,
                      new HttpMessage(message.getId(), message.getHeaders(), message.getPayload()));
            });

    messageSubscriptions.put(subscriptionInstanceId, new SubscriptionUpdateTime(messageSubscription, System.currentTimeMillis()));

    return subscriptionInstanceId;
  }

  @RequestMapping(value = "/subscriptions/{subscriptionInstanceId}/heartbeat", method = RequestMethod.POST)
  public void heartbeat(@PathVariable(name = "subscriptionInstanceId") String subscriptionInstanceId) {
    Optional.ofNullable(messageSubscriptions.get(subscriptionInstanceId))
            .ifPresent(subscriptionUpdateTime -> subscriptionUpdateTime.setUpdateTime(System.currentTimeMillis()));
  }

  @RequestMapping(value = "/subscriptions/{subscriptionInstanceId}", method = RequestMethod.DELETE)
  public void unsubscribe(@PathVariable(name = "subscriptionInstanceId") String subscriptionInstanceId) {
    Optional
            .ofNullable(messageSubscriptions.remove(subscriptionInstanceId))
            .map(SubscriptionUpdateTime::getSubscription)
            .ifPresent(MessageSubscription::unsubscribe);
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }

  private static class SubscriptionUpdateTime {
    private MessageSubscription subscription;
    private long updateTime;

    public SubscriptionUpdateTime(MessageSubscription subscription, long updateTime) {
      this.subscription = subscription;
      this.updateTime = updateTime;
    }

    public MessageSubscription getSubscription() {
      return subscription;
    }

    public void setSubscription(MessageSubscription subscription) {
      this.subscription = subscription;
    }

    public long getUpdateTime() {
      return updateTime;
    }

    public void setUpdateTime(long updateTime) {
      this.updateTime = updateTime;
    }
  }
}
