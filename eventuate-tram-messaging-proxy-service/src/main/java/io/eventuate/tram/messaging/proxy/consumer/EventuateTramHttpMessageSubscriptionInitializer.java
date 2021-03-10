package io.eventuate.tram.messaging.proxy.consumer;

import io.eventuate.tram.messaging.proxy.service.SubscriptionService;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.stream.Collectors;

public class EventuateTramHttpMessageSubscriptionInitializer {
  private EventuateSubscriptionProperties eventuateSubscriptionProperties;
  private SubscriptionService subscriptionService;

  public EventuateTramHttpMessageSubscriptionInitializer(EventuateSubscriptionProperties eventuateSubscriptionProperties,
                                                         SubscriptionService subscriptionService) {

    this.eventuateSubscriptionProperties = eventuateSubscriptionProperties;
    this.subscriptionService = subscriptionService;
  }

  @PostConstruct
  public void subscribe() {
    subscribeToMessages();
    subscribeToEvents();
  }

  private void subscribeToMessages() {
    eventuateSubscriptionProperties.getMessage().keySet().forEach(subscriberId -> {
      MessageSubscriptionData messageSubscriptionData = eventuateSubscriptionProperties.getMessage().get(subscriberId);

      subscriptionService.subscribeToMessage(subscriberId,
              Arrays.stream(messageSubscriptionData.getChannels().split(",")).collect(Collectors.toSet()),
              messageSubscriptionData.getUrl(),
              subscriberId);
    });
  }

  private void subscribeToEvents() {
    eventuateSubscriptionProperties.getEvent().keySet().forEach(subscriberId -> {
      EventSubscriptionData eventSubscriptionData = eventuateSubscriptionProperties.getEvent().get(subscriberId);

      subscriptionService.subscribeToEvent(subscriberId,
              eventSubscriptionData.getAggregate(),
              Arrays.stream(eventSubscriptionData.getEvents().split(",")).collect(Collectors.toSet()),
              eventSubscriptionData.getBaseUrl());
    });
  }
}
