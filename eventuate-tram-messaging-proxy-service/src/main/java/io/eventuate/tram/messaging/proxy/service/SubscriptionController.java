package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.http.common.HttpMessage;
import io.eventuate.tram.consumer.http.common.SubscribeRequest;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
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
  public void subscribe(@RequestBody SubscribeRequest subscribeRequest) {
    MessageSubscription messageSubscription = messageConsumerImplementation.subscribe(subscribeRequest.getSubscriberId(),
            subscribeRequest.getChannels(),
            message -> {
              restTemplate.postForLocation(subscribeRequest.getCallbackUrl() + "/" + subscribeRequest.getSubscriberId(),
                      new HttpMessage(message.getId(), message.getHeaders(), message.getPayload()));
            });

    messageSubscriptions.put(subscribeRequest.getSubscriberId(), messageSubscription);
  }

  @RequestMapping(value = "/subscriptions/{subscriberId}", method = RequestMethod.DELETE)
  public void unsubscribe(@PathVariable(name = "subscriberId") String subscriberId) {
    Optional
            .ofNullable(messageSubscriptions.remove(subscriberId))
            .ifPresent(MessageSubscription::unsubscribe);
  }

}
