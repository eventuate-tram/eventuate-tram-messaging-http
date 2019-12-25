package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.http.common.HttpMessage;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SubscriptionService {
  private RestTemplate restTemplate;
  private MessageConsumerImplementation messageConsumerImplementation;
  private int maxHeartbeatInterval;

  public SubscriptionService(RestTemplate restTemplate, MessageConsumerImplementation messageConsumerImplementation, int maxHeartbeatInterval) {
    this.restTemplate = restTemplate;
    this.messageConsumerImplementation = messageConsumerImplementation;
    this.maxHeartbeatInterval = maxHeartbeatInterval;
  }

  private ConcurrentMap<String, SubscriptionUpdateTime> messageSubscriptions = new ConcurrentHashMap<>();

  public String subscribe(String subscriberId, Set<String> channels, String callbackUrl, Optional<String> optionalSubscriptionInstanceId) {
    String subscriptionInstanceId = optionalSubscriptionInstanceId.orElseGet(() -> generateId());

    MessageSubscription messageSubscription = messageConsumerImplementation.subscribe(subscriberId,
            channels,
            message -> {

              long lastUpdateTime = Optional
                      .ofNullable(messageSubscriptions.get(subscriptionInstanceId))
                      .map(SubscriptionUpdateTime::getUpdateTime)
                      .orElse(System.currentTimeMillis());

              if (System.currentTimeMillis() - lastUpdateTime > maxHeartbeatInterval) {
                unsubscribe(subscriptionInstanceId);
                throw new RuntimeException("Heartbeat timeout.");
              }

              restTemplate.postForLocation(callbackUrl + "/" + subscriptionInstanceId,
                      new HttpMessage(message.getId(), message.getHeaders(), message.getPayload()));
            });

    messageSubscriptions.put(subscriptionInstanceId, new SubscriptionUpdateTime(messageSubscription, System.currentTimeMillis()));

    return subscriptionInstanceId;
  }

  public void update(String subscriptionInstanceId) {
    Optional.ofNullable(messageSubscriptions.get(subscriptionInstanceId))
            .ifPresent(subscriptionUpdateTime -> subscriptionUpdateTime.setUpdateTime(System.currentTimeMillis()));
  }

  public void unsubscribe(String subscriptionInstanceId) {
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
