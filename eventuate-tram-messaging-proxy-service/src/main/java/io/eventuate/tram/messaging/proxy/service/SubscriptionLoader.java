package io.eventuate.tram.messaging.proxy.service;

import javax.annotation.PostConstruct;
import java.util.Optional;

public class SubscriptionLoader {

  private SubscriptionService subscriptionService;
  private SubscriptionPersistenceService subscriptionPersistenceService;

  public SubscriptionLoader(SubscriptionService subscriptionService, SubscriptionPersistenceService subscriptionPersistenceService) {
    this.subscriptionService = subscriptionService;
    this.subscriptionPersistenceService = subscriptionPersistenceService;
  }

  @PostConstruct
  public void load() {
    subscriptionPersistenceService
            .loadSubscriptionInfos()
            .forEach(subscriptionInfo ->
                    subscriptionService.subscribe(subscriptionInfo.getSubscriberId(),
                            subscriptionInfo.getChannels(),
                            subscriptionInfo.getCallbackUrl(),
                            Optional.of(subscriptionInfo.getSubscriptionInstanceId())));
  }
}
