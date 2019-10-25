package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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

  private ConcurrentMap<String, MessageSubscription> messageSubscriptions = new ConcurrentHashMap<>();

  @RequestMapping(value = "/subscriptions", method = RequestMethod.POST)
  public String subscribe(@RequestBody SubscribeRequest subscribeRequest) {
    String subscriptionId = UUID.randomUUID().toString();

    MessageSubscription messageSubscription = messageConsumerImplementation.subscribe(subscribeRequest.getSubscriberId(),
            subscribeRequest.getChannels(),
            message ->
                    restTemplate.postForLocation(subscribeRequest.getCallbackUrl() + "/" + subscriptionId,
                            new MessageResponse(message.getId(), message.getHeaders(), message.getPayload())));

    messageSubscriptions.put(subscriptionId, messageSubscription);

    return subscriptionId;
  }

  @RequestMapping(value = "/subscriptions/{subscriptionId}", method = RequestMethod.DELETE)
  public void unsubscribe(@PathVariable(name = "subscriptionId") String subscriptionId) {
    Optional
            .ofNullable(messageSubscriptions.remove(subscriptionId))
            .ifPresent(MessageSubscription::unsubscribe);
  }

}
