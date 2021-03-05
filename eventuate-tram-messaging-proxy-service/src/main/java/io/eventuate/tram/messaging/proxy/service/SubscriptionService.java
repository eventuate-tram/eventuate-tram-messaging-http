package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.http.common.HttpMessage;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SubscriptionService {
  private SubscriptionPersistenceService subscriptionPersistenceService;
  private SubscriptionRequestManager subscriptionRequestManager;
  private RestTemplate restTemplate;
  private MessageConsumerImplementation messageConsumerImplementation;

  public SubscriptionService(SubscriptionPersistenceService subscriptionPersistenceService,
                             SubscriptionRequestManager subscriptionRequestManager,
                             RestTemplate restTemplate,
                             MessageConsumerImplementation messageConsumerImplementation) {
    this.subscriptionPersistenceService = subscriptionPersistenceService;
    this.subscriptionRequestManager = subscriptionRequestManager;
    this.restTemplate = restTemplate;
    this.messageConsumerImplementation = messageConsumerImplementation;
  }

  private ConcurrentMap<String, MessageSubscription> messageSubscriptions = new ConcurrentHashMap<>();

  public String makeSubscriptionRequest(String subscriberId,
                          Set<String> channels,
                          String callbackUrl,
                          Optional<String> callbackSubscriptionId) {

    String subscriptionInstanceId = generateId();

    subscriptionRequestManager.createSubscriptionRequest(new SubscriptionInfo(subscriptionInstanceId,
            subscriberId, channels, callbackUrl, callbackSubscriptionId.orElse(null)));

    subscriptionPersistenceService.saveSubscriptionInfo(new SubscriptionInfo(subscriptionInstanceId,
            subscriberId, channels, callbackUrl, callbackSubscriptionId.orElse(null)));

    return subscriptionInstanceId;
  }

  public String subscribe(String subscriberId,
                          Set<String> channels,
                          String callbackUrl,
                          Optional<String> callbackSubscriptionId,
                          String subscriptionInstanceId) {

    messageSubscriptions.computeIfAbsent(subscriptionInstanceId, instanceId -> {
      MessageSubscription messageSubscription = messageConsumerImplementation.subscribe(subscriberId,
              channels,
              message ->
                restTemplate.postForLocation(callbackUrl + "/" + callbackSubscriptionId.orElse(subscriptionInstanceId),
                        new HttpMessage(message.getId(), message.getHeaders(), message.getPayload())));

      return messageSubscription;
    });

    return subscriptionInstanceId;
  }

  public void updateSubscription(String subscriptionInstanceId) {
    Optional
            .ofNullable(messageSubscriptions.get(subscriptionInstanceId))
            .ifPresent(subscription -> subscriptionRequestManager.touch(subscriptionInstanceId));
  }

  public void makeUnsubscriptionRequest(String subscriptionInstanceId) {
    subscriptionRequestManager.removeSubscriptionRequest(subscriptionInstanceId);
  }

  public void unsubscribe(String subscriptionInstanceId) {
    Optional
            .ofNullable(messageSubscriptions.remove(subscriptionInstanceId))
            .ifPresent(MessageSubscription::unsubscribe);

    subscriptionPersistenceService.deleteSubscriptionInfo(subscriptionInstanceId);
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
