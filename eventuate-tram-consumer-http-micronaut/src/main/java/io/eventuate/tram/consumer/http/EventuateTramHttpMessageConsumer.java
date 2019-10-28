package io.eventuate.tram.consumer.http;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import io.eventuate.tram.messaging.http.SubscribeRequest;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EventuateTramHttpMessageConsumer implements MessageConsumerImplementation {

  private final String id = UUID.randomUUID().toString();

  private EventuateTramHttpMessageController eventuateTramHttpMessageController;
  private String httpProxyBaseUrl;
  private String httpConsumerBaseUrl;
  private RestTemplate restTemplate = new RestTemplate();
  private Set<String> subscriptions = new HashSet<>();

  public EventuateTramHttpMessageConsumer(EventuateTramHttpMessageController eventuateTramHttpMessageController,
                                          String httpProxyBaseUrl,
                                          String httpConsumerBaseUrl) {

    this.eventuateTramHttpMessageController = eventuateTramHttpMessageController;
    this.httpProxyBaseUrl = httpProxyBaseUrl;
    this.httpConsumerBaseUrl = httpConsumerBaseUrl;
  }

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {

    restTemplate.postForLocation(httpProxyBaseUrl,
            new SubscribeRequest(subscriberId, channels, httpConsumerBaseUrl), String.class);

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
    restTemplate.delete(httpProxyBaseUrl + "/" + subscriberId);
  }
}
