package io.eventuate.tram.messaging.proxy.consumer;

import io.eventuate.tram.messaging.proxy.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class EventuateTramHttpMessageSubscriptionInitializer {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventuateSubscriptionProperties eventuateSubscriptionProperties;
  private SubscriptionService subscriptionService;

  public EventuateTramHttpMessageSubscriptionInitializer(EventuateSubscriptionProperties eventuateSubscriptionProperties,
                                                         SubscriptionService subscriptionService) {

    this.eventuateSubscriptionProperties = eventuateSubscriptionProperties;
    this.subscriptionService = subscriptionService;
  }

  @PostConstruct
  public void subscribe() {
    logger.info("EventuateTramHttpMessageSubscriptionInitializer.subscribe() started");

    subscribeToMessages();
    subscribeToEvents();
    subscribeToCommands();
    subscribeToReplies();

    logger.info("EventuateTramHttpMessageSubscriptionInitializer.subscribe() finished");
  }

  private void subscribeToMessages() {
    logger.info("EventuateTramHttpMessageSubscriptionInitializer.subscribeToMessages() started");

    eventuateSubscriptionProperties.getMessage().keySet().forEach(subscriberId -> {
      MessageSubscriptionData messageSubscriptionData = eventuateSubscriptionProperties.getMessage().get(subscriberId);

      logger.info("Subscribing to message {} with subscriber id {}", messageSubscriptionData, subscriberId);

      subscriptionService.subscribeToMessage(subscriberId,
              stringToSet(messageSubscriptionData.getChannels()),
              messageSubscriptionData.getBaseUrl(),
              subscriberId);

      logger.info("Subscribed to message {} with id {}", messageSubscriptionData, subscriberId);
    });

    logger.info("EventuateTramHttpMessageSubscriptionInitializer.subscribeToMessages() finished");
  }

  private void subscribeToEvents() {
    logger.info("EventuateTramHttpMessageSubscriptionInitializer.subscribeToEvents() started");

    eventuateSubscriptionProperties.getEvent().keySet().forEach(subscriberId -> {
      EventSubscriptionData eventSubscriptionData = eventuateSubscriptionProperties.getEvent().get(subscriberId);

      logger.info("Subscribing to event {} with subscriber id {}", eventSubscriptionData, subscriberId);

      subscriptionService.subscribeToEvent(subscriberId,
              eventSubscriptionData.getAggregate(),
              stringToSet(eventSubscriptionData.getEvents()),
              eventSubscriptionData.getBaseUrl());

      logger.info("Subscribed to event {} with subscriber id {}", eventSubscriptionData, subscriberId);
    });

    logger.info("EventuateTramHttpMessageSubscriptionInitializer.subscribeToEvents() finished");
  }

  private void subscribeToCommands() {
    logger.info("EventuateTramHttpMessageSubscriptionInitializer.subscribeToCommands() started");

    eventuateSubscriptionProperties.getCommand().keySet().forEach(dispatcherId -> {
      CommandSubscriptionData commandSubscriptionData = eventuateSubscriptionProperties.getCommand().get(dispatcherId);

      logger.info("Subscribing to command {} with dispatcher id {}", commandSubscriptionData, dispatcherId);

      subscriptionService.subscribeToCommand(dispatcherId,
              commandSubscriptionData.getChannel(),
              Optional.ofNullable(commandSubscriptionData.getResource()),
              stringToSet(commandSubscriptionData.getCommands()),
              commandSubscriptionData.getBaseUrl());

      logger.info("Subscribed to command {} with dispatcher id {}", commandSubscriptionData, dispatcherId);
    });

    logger.info("EventuateTramHttpMessageSubscriptionInitializer.subscribeToCommands() finished");
  }

  private void subscribeToReplies() {
    logger.info("EventuateTramHttpMessageSubscriptionInitializer.subscribeToReplies() started");

    eventuateSubscriptionProperties.getReply().keySet().forEach(subscriberId -> {
      ReplySubscriptionData replySubscriptionData = eventuateSubscriptionProperties.getReply().get(subscriberId);

      logger.info("Subscribing to reply {} with subscriber id {}", replySubscriptionData, subscriberId);

      subscriptionService.subscribeToReply(subscriberId,
              replySubscriptionData.getReplyChannel(),
              Optional.ofNullable(replySubscriptionData.getResource()),
              stringToSet(replySubscriptionData.getCommands()),
              replySubscriptionData.getBaseUrl());

      logger.info("Subscribed to reply {} with subscriber id {}", replySubscriptionData, subscriberId);
    });

    logger.info("EventuateTramHttpMessageSubscriptionInitializer.subscribeToReplies() finished");
  }

  private Set<String> stringToSet(String value) {
    return Arrays.stream(value.split(",")).collect(Collectors.toSet());
  }
}
