package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.http.common.HttpMessage;
import io.eventuate.tram.consumer.http.common.SubscriptionType;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

  public String makeSubscriptionRequest(SubscriptionType subscriptionType,
                                        String subscriberId,
                                        Set<String> channels,
                                        String callbackUrl,
                                        boolean discardSubscriptionIdInCallbackUrl) {

    String subscriptionInstanceId = generateId();

    subscriptionRequestManager.createSubscriptionRequest(new SubscriptionInfo(subscriptionType,
            subscriptionInstanceId, subscriberId, channels, callbackUrl, discardSubscriptionIdInCallbackUrl));

    subscriptionPersistenceService.saveSubscriptionInfo(new SubscriptionInfo(subscriptionType,
            subscriptionInstanceId, subscriberId, channels, callbackUrl, discardSubscriptionIdInCallbackUrl));

    return subscriptionInstanceId;
  }

  public String subscribe(SubscriptionType subscriptionType,
                          String subscriberId,
                          Set<String> channels,
                          String callbackUrl,
                          String subscriptionInstanceId,
                          boolean discardSubscriptionIdToCallbackUrl) {

    messageSubscriptions.computeIfAbsent(subscriptionInstanceId, instanceId -> {
      MessageSubscription messageSubscription = messageConsumerImplementation.subscribe(subscriberId,
              channels,
              message -> publish(message, callbackUrl, subscriptionInstanceId, subscriptionType, discardSubscriptionIdToCallbackUrl));

      return messageSubscription;
    });

    return subscriptionInstanceId;
  }

  private void publish(Message message,
                       String callbackUrl,
                       String subscriptionInstanceId,
                       SubscriptionType subscriptionType,
                       boolean discardSubscriptionIdToCallbackUrl) {
    String location = callbackUrl + (discardSubscriptionIdToCallbackUrl ? "" : "/" + subscriptionInstanceId);
    Object request = null;

    switch (subscriptionType) {
      case MESSAGE: {
        request = new HttpMessage(message.getId(), message.getHeaders(), message.getPayload());
        break;
      }
      case EVENT: {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        request = new HttpEntity<>(message.getPayload(), headers);
        break;
      }
    }

    restTemplate.postForLocation(location, request);
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
