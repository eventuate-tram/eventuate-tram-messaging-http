package io.eventuate.tram.messaging.proxy.consumer;

import io.eventuate.tram.messaging.proxy.service.SubscriptionService;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
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
    subscribeToCommands();
    subscribeToReplies();
  }

  private void subscribeToMessages() {
    eventuateSubscriptionProperties.getMessage().keySet().forEach(subscriberId -> {
      MessageSubscriptionData messageSubscriptionData = eventuateSubscriptionProperties.getMessage().get(subscriberId);

      subscriptionService.subscribeToMessage(subscriberId,
              stringToSet(messageSubscriptionData.getChannels()),
              messageSubscriptionData.getBaseUrl(),
              subscriberId);
    });
  }

  private void subscribeToEvents() {
    eventuateSubscriptionProperties.getEvent().keySet().forEach(subscriberId -> {
      EventSubscriptionData eventSubscriptionData = eventuateSubscriptionProperties.getEvent().get(subscriberId);

      subscriptionService.subscribeToEvent(subscriberId,
              eventSubscriptionData.getAggregate(),
              stringToSet(eventSubscriptionData.getEvents()),
              eventSubscriptionData.getBaseUrl());
    });
  }

  private void subscribeToCommands() {
    eventuateSubscriptionProperties.getCommand().keySet().forEach(dispatcherId -> {
      CommandSubscriptionData commandSubscriptionData = eventuateSubscriptionProperties.getCommand().get(dispatcherId);

      subscriptionService.subscribeToCommand(dispatcherId,
              commandSubscriptionData.getChannel(),
              Optional.ofNullable(commandSubscriptionData.getResource()),
              stringToSet(commandSubscriptionData.getCommands()),
              commandSubscriptionData.getBaseUrl());
    });
  }

  private void subscribeToReplies() {
    eventuateSubscriptionProperties.getReply().keySet().forEach(subscriberId -> {
      ReplySubscriptionData replySubscriptionData = eventuateSubscriptionProperties.getReply().get(subscriberId);

      subscriptionService.subscribeToReply(subscriberId,
              replySubscriptionData.getReplyChannel(),
              Optional.ofNullable(replySubscriptionData.getResource()),
              stringToSet(replySubscriptionData.getCommands()),
              replySubscriptionData.getBaseUrl());
    });
  }

  private Set<String> stringToSet(String value) {
    return Arrays.stream(value.split(",")).collect(Collectors.toSet());
  }
}
