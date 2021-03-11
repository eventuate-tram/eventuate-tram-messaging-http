package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.http.common.HttpMessage;
import io.eventuate.tram.events.common.EventMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
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
                                        String callbackUrl) {

    String subscriptionInstanceId = generateId();

    subscriptionRequestManager.createSubscriptionRequest(new SubscriptionInfo(subscriptionInstanceId,
            subscriberId, channels, callbackUrl));

    subscriptionPersistenceService.saveSubscriptionInfo(new SubscriptionInfo(subscriptionInstanceId,
            subscriberId, channels, callbackUrl));

    return subscriptionInstanceId;
  }

  public String subscribeToEvent(String subscriberId,
                                 String aggregate,
                                 Set<String> events,
                                 String callbackUrl) {
    messageSubscriptions.computeIfAbsent(subscriberId, instanceId -> {
      MessageSubscription messageSubscription = messageConsumerImplementation.subscribe(subscriberId,
              Collections.singleton(aggregate),
              message -> publishEvent(message, aggregate, events, callbackUrl, subscriberId));

      return messageSubscription;
    });

    return subscriberId;
  }

  public String subscribeToMessage(String subscriberId,
                                   Set<String> channels,
                                   String callbackUrl,
                                   String subscriptionInstanceId) {
    messageSubscriptions.computeIfAbsent(subscriptionInstanceId, instanceId -> {
      MessageSubscription messageSubscription = messageConsumerImplementation.subscribe(subscriberId,
              channels,
              message -> publishMessage(message, callbackUrl, subscriptionInstanceId));

      return messageSubscription;
    });

    return subscriptionInstanceId;
  }

  private void publishMessage(Message message,
                              String callbackUrl,
                              String subscriptionInstanceId) {
    String location = callbackUrl + "/" + subscriptionInstanceId;

    restTemplate.postForLocation(location, new HttpMessage(message.getId(), message.getHeaders(), message.getPayload()));
  }

  private void publishEvent(Message message,
                            String aggregate,
                            Set<String> events,
                            String callbackUrl,
                            String subscriberId) {

    String event = message.getRequiredHeader(EventMessageHeaders.EVENT_TYPE);

    if (!events.contains(event)) {
      return;
    }

    String location = String.format("%s/%s/%s/%s/%s/%s",
            callbackUrl,
            subscriberId,
            aggregate,
            message.getRequiredHeader(EventMessageHeaders.AGGREGATE_ID),
            event,
            message.getRequiredHeader(Message.ID));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    restTemplate.postForLocation(location, new HttpEntity<>(message.getPayload(), headers));
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
