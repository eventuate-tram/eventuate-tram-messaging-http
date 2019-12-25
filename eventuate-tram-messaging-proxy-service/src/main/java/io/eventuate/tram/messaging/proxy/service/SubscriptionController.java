package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.tram.consumer.http.common.SubscribeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
public class SubscriptionController {

  @Autowired
  private SubscriptionService subscriptionService;

  @Autowired
  private SubscriptionPersistenceService subscriptionPersistenceService;

  @RequestMapping(value = "/subscriptions", method = RequestMethod.POST)
  public String subscribe(@RequestBody SubscribeRequest subscribeRequest) {
    String subscriptionInstanceId = subscriptionService.subscribe(subscribeRequest.getSubscriberId(),
            subscribeRequest.getChannels(), subscribeRequest.getCallbackUrl(), Optional.empty());

    subscriptionPersistenceService.saveSubscriptionInfo(new SubscriptionInfo(subscriptionInstanceId,
            subscribeRequest.getSubscriberId(), subscribeRequest.getChannels(), subscribeRequest.getCallbackUrl()));

    return subscriptionInstanceId;
  }

  @RequestMapping(value = "/subscriptions/{subscriptionInstanceId}/heartbeat", method = RequestMethod.POST)
  public void heartbeat(@PathVariable(name = "subscriptionInstanceId") String subscriptionInstanceId) {
    subscriptionService.update(subscriptionInstanceId);
  }

  @RequestMapping(value = "/subscriptions/{subscriptionInstanceId}", method = RequestMethod.DELETE)
  public void unsubscribe(@PathVariable(name = "subscriptionInstanceId") String subscriptionInstanceId) {
    subscriptionService.unsubscribe(subscriptionInstanceId);
    subscriptionPersistenceService.deleteSubscriptionInfo(subscriptionInstanceId);
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
