package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.commands.common.CommandReplyOutcome;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.commands.common.paths.ResourcePath;
import io.eventuate.tram.commands.common.paths.ResourcePathPattern;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.http.common.EventuateHttpHeaders;
import io.eventuate.tram.consumer.http.common.HttpMessage;
import io.eventuate.tram.events.common.EventMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class SubscriptionService {
  private Logger logger = LoggerFactory.getLogger(getClass());

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

  public void subscribeToReply(String subscriberId,
                               String replyChannel,
                               Optional<String> resource,
                               Set<String> commands,
                               String callbackUrl) {
    messageSubscriptions.computeIfAbsent(subscriberId, instanceId -> {
      MessageSubscription messageSubscription = messageConsumerImplementation.subscribe(subscriberId,
              Collections.singleton(replyChannel),
              message -> publishReply(message, callbackUrl, subscriberId, commands, resource));

      return messageSubscription;
    });
  }

  private void publishReply(Message message,
                            String callbackUrl,
                            String subscriberId,
                            Set<String> commands,
                            Optional<String> resource) {
    logger.debug("publishing reply {}", message);

    String command = message.getRequiredHeader(CommandMessageHeaders.inReply(CommandMessageHeaders.COMMAND_TYPE));

    if (!commands.contains(command)) {
      return;
    }

    if (!shouldPublishResource(resource, message.getHeader(CommandMessageHeaders.inReply(CommandMessageHeaders.RESOURCE)))) {
      return;
    }

    String location = String.format("%s/%s/%s/%s/%s/%s%s",
            callbackUrl,
            subscriberId,
            command,
            message.getRequiredHeader(ReplyMessageHeaders.IN_REPLY_TO),
            message.getRequiredHeader(ReplyMessageHeaders.REPLY_TYPE),
            message.getRequiredHeader(ReplyMessageHeaders.REPLY_OUTCOME),
            message.getHeader(CommandMessageHeaders.inReply(CommandMessageHeaders.RESOURCE)).orElse(""));

    logger.debug("sending reply {} to location {}", message, location);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    restTemplate.postForLocation(location, new HttpEntity<>(message.getPayload(), headers));

    logger.debug("sent reply {} to location {}", message, location);
  }

  public String subscribeToCommand(String commandDispatcherId,
                                   String channel,
                                   Optional<String> resource,
                                   Set<String> commands,
                                   String callbackUrl) {
    messageSubscriptions.computeIfAbsent(commandDispatcherId, instanceId -> {
      MessageSubscription messageSubscription = messageConsumerImplementation.subscribe(commandDispatcherId,
              Collections.singleton(channel),
              message -> publishCommand(message, commandDispatcherId, resource, commands, callbackUrl));

      return messageSubscription;
    });

    return commandDispatcherId;
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
              message -> publishMessage(message, callbackUrl, subscriberId, subscriptionInstanceId));

      return messageSubscription;
    });

    return subscriptionInstanceId;
  }

  private void publishMessage(Message message,
                              String callbackUrl,
                              String subscriberId,
                              String subscriptionInstanceId) {
    String location = callbackUrl + "/" + subscriptionInstanceId;

    logger.debug("sending message {} to location {}", message, location);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    addCommonHeaders(headers, subscriberId, message.getId());

    HttpMessage httpMessage = new HttpMessage(message.getId(), message.getHeaders(), message.getPayload());

    restTemplate.postForLocation(location, new HttpEntity<>(httpMessage, headers));

    logger.debug("sent message {} to location {}", message, location);
  }

  private void publishEvent(Message message,
                            String aggregate,
                            Set<String> events,
                            String callbackUrl,
                            String subscriberId) {

    logger.debug("publishing event {}", message);

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

    logger.debug("sending event {} to location {}", message, location);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    addCommonHeaders(headers, subscriberId, message.getId());
    restTemplate.postForLocation(location, new HttpEntity<>(message.getPayload(), headers));

    logger.debug("sent event {} to location {}", message, location);
  }

  private void publishCommand(Message message,
                              String commandDispatcherId,
                              Optional<String> resource,
                              Set<String> commands,
                              String callbackUrl) {

    logger.debug("publishing command {}", message);

    String command = message.getRequiredHeader(CommandMessageHeaders.COMMAND_TYPE);

    if (!commands.contains(command)) {
      return;
    }

    if (!shouldPublishResource(resource, message.getHeader(CommandMessageHeaders.RESOURCE))) {
      return;
    }

    String replyChannel = message.getRequiredHeader(CommandMessageHeaders.REPLY_TO);

    String location =
            String.format("%s/%s/%s/%s/%s%s",
                    callbackUrl,
                    commandDispatcherId,
                    message.getId(),
                    command,
                    replyChannel,
                    resource.isPresent() ? message.getRequiredHeader(CommandMessageHeaders.RESOURCE) : "");

    logger.debug("sending command {} to location {}", message, location);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    Map<String, String> correlationHeaders = correlationHeaders(message.getHeaders());
    headers.add(EventuateHttpHeaders.COMMAND_REPLY_HEADERS, JSonMapper.toJson(correlationHeaders));
    addCommonHeaders(headers, commandDispatcherId, message.getId());
    restTemplate.postForLocation(location, new HttpEntity<>(message.getPayload(), headers));

    logger.debug("sent command {} to location {}", message, location);
  }

  private boolean shouldPublishResource(Optional<String> resource, Optional<String> messageResource) {
    if (resource.isPresent()) {
      return messageResource
              .map(mr -> {
                ResourcePathPattern resourcePathPattern = ResourcePathPattern.parse(resource.get());
                ResourcePath resourcePath = ResourcePath.parse(mr);
                return resourcePathPattern.isSatisfiedBy(resourcePath);
              })
              .orElse(false);

    }

    return true;
  }

  private Map<String, String> correlationHeaders(Map<String, String> headers) {
    Map<String, String> m = headers.entrySet()
            .stream()
            .filter(e -> e.getKey().startsWith(CommandMessageHeaders.COMMAND_HEADER_PREFIX))
            .collect(Collectors.toMap(e -> CommandMessageHeaders.inReply(e.getKey()),
                    Map.Entry::getValue));
    m.put(ReplyMessageHeaders.IN_REPLY_TO, headers.get(Message.ID));
    return m;
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

  private void addCommonHeaders(HttpHeaders headers, String subscriberId, String messageId) {
    headers.add(EventuateHttpHeaders.SUBSCRIBER_ID, subscriberId);
    headers.add(EventuateHttpHeaders.MESSAGE_ID, messageId);
  }
}
