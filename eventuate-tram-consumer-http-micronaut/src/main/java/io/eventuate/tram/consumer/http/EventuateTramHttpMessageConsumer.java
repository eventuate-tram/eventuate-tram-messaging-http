package io.eventuate.tram.consumer.http;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.http.common.SubscribeRequest;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EventuateTramHttpMessageConsumer implements MessageConsumerImplementation {

  private final String id = UUID.randomUUID().toString();

  private EventuateTramHttpMessageController eventuateTramHttpMessageController;
  private String httpConsumerBaseUrl;
  private ProxyClient proxyClient;
  private HeartbeatService heartbeatService;
  private Set<String> subscriptions = new HashSet<>();

  public EventuateTramHttpMessageConsumer(ProxyClient proxyClient,
                                          HeartbeatService heartbeatService,
                                          EventuateTramHttpMessageController eventuateTramHttpMessageController,
                                          String httpConsumerBaseUrl) {

    this.proxyClient = proxyClient;
    this.heartbeatService = heartbeatService;
    this.eventuateTramHttpMessageController = eventuateTramHttpMessageController;
    this.httpConsumerBaseUrl = httpConsumerBaseUrl;
  }

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {

    String subscriptionInstanceId = proxyClient.subscribe(new SubscribeRequest(subscriberId, channels, httpConsumerBaseUrl));

    heartbeatService.addSubscription(subscriptionInstanceId);

    subscriptions.add(subscriptionInstanceId);

    eventuateTramHttpMessageController.addSubscriptionHandler(subscriptionInstanceId, handler);

    return () -> {
      subscriptions.remove(subscriptionInstanceId);
      unsubscribe(subscriptionInstanceId);
    };
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void close() {
    subscriptions.forEach(this::unsubscribe);
  }

  private void unsubscribe(String subscriptionInstanceId) {
    heartbeatService.removeSubscription(subscriptionInstanceId);
    eventuateTramHttpMessageController.removeSubscriptionHandler(subscriptionInstanceId);
    proxyClient.unsubscribe(subscriptionInstanceId);
  }
}
