package io.eventuate.tram.consumer.http;

import io.eventuate.tram.consumer.http.common.HttpMessage;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Controller
public class EventuateTramHttpMessageController {

  private ConcurrentMap<String, MessageHandler> messageHandlers = new ConcurrentHashMap<>();

  @Post(value = "/messages/{subscriberId}")
  public void messageReceived(HttpMessage httpMessage, String subscriberId) {
    Optional
            .ofNullable(messageHandlers.get(subscriberId))
            .ifPresent(messageHandler ->
                messageHandler.accept(new MessageImpl(httpMessage.getPayload(), httpMessage.getHeaders())));
  }

  public void addSubscriptionHandler(String subscriberId, MessageHandler messageHandler) {
    messageHandlers.put(subscriberId, messageHandler);
  }

  public void removeSubscriptionHandler(String subscriberId) {
    messageHandlers.remove(subscriberId);
  }
}
