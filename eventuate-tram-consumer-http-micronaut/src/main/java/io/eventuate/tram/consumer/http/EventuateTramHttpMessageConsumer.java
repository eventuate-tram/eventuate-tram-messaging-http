package io.eventuate.tram.consumer.http;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
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
  private Set<String> subscriptions = new HashSet<>();

  public EventuateTramHttpMessageConsumer(ProxyClient proxyClient,
                                          EventuateTramHttpMessageController eventuateTramHttpMessageController,
                                          String httpConsumerBaseUrl) {

    this.proxyClient = proxyClient;
    this.eventuateTramHttpMessageController = eventuateTramHttpMessageController;
    this.httpConsumerBaseUrl = httpConsumerBaseUrl;
  }

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {

    proxyClient.subscribe(new SubscribeRequest(subscriberId, channels, httpConsumerBaseUrl));

    subscriptions.add(subscriberId);

    eventuateTramHttpMessageController.addSubscriptionHandler(subscriberId, handler);

    return () -> {
      subscriptions.remove(subscriberId);
      unsubscribe(subscriberId);
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

  private void unsubscribe(String subscriberId) {
    eventuateTramHttpMessageController.removeSubscriptionHandler(subscriberId);
    proxyClient.unsubscribe(subscriberId);
  }
}
