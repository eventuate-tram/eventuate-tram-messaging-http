package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
public class SubscriptionController {

  @Autowired
  private MessageConsumerImplementation messageConsumerImplementation;

  @Autowired
  private RestTemplate restTemplate;

  private ConcurrentMap<SubscriberIdAndChannels, MessageSubscription> messageSubscriptions = new ConcurrentHashMap<>();

  @RequestMapping(value = "/subscriptions", method = RequestMethod.POST)
  public void subscribe(@RequestBody SubscribeRequest subscribeRequest) {
    SubscriberIdAndChannels subscriberIdAndChannels = new SubscriberIdAndChannels(subscribeRequest.getSubscriberId(), subscribeRequest.getChannels());

    synchronized (messageSubscriptions) {
      Optional
              .ofNullable(messageSubscriptions.remove(subscriberIdAndChannels))
              .ifPresent(MessageSubscription::unsubscribe);

      MessageSubscription messageSubscription = messageConsumerImplementation.subscribe(subscribeRequest.getSubscriberId(),
              subscribeRequest.getChannels(),
              message ->
                      restTemplate.postForLocation(subscribeRequest.getCallbackUrl(),
                              new MessageResponse(message.getId(), message.getHeaders(), message.getPayload())));

      messageSubscriptions.put(subscriberIdAndChannels, messageSubscription);
    }
  }

  @RequestMapping(value = "/subscriptions/unsubscribe", method = RequestMethod.POST)
  public void unsubscribe(@RequestBody UnsubscribeRequest unsubscribeRequest) {
    SubscriberIdAndChannels subscriberIdAndChannels = new SubscriberIdAndChannels(unsubscribeRequest.getSubscriberId(), unsubscribeRequest.getChannels());

    Optional
            .ofNullable(messageSubscriptions.remove(subscriberIdAndChannels))
            .ifPresent(MessageSubscription::unsubscribe);
  }

}
