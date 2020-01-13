package io.eventuate.tram.messaging.proxy.service;

import javax.annotation.PostConstruct;
import java.util.Optional;

public class SubscriptionLoader {

  private SubscriptionService subscriptionService;
  private SubscriptionPersistenceService subscriptionPersistenceService;
  private SubscriptionRequestManager subscriptionRequestManager;

  public SubscriptionLoader(SubscriptionService subscriptionService,
                            SubscriptionPersistenceService subscriptionPersistenceService,
                            SubscriptionRequestManager subscriptionRequestManager) {
    this.subscriptionService = subscriptionService;
    this.subscriptionPersistenceService = subscriptionPersistenceService;
    this.subscriptionRequestManager = subscriptionRequestManager;
  }

  @PostConstruct
  public void load() {
    loadPersistentSubscriptions();
    followToSubscriptions();
  }

  private void loadPersistentSubscriptions() {
    subscriptionPersistenceService
            .loadSubscriptionInfos()
            .forEach(subscriptionInfo ->
                    subscriptionService.subscribe(subscriptionInfo.getSubscriberId(),
                            subscriptionInfo.getChannels(),
                            subscriptionInfo.getCallbackUrl(),
                            Optional.of(subscriptionInfo.getSubscriptionInstanceId()),
                            subscriptionInfo.isFollower()));
  }

  private void followToSubscriptions() {
    subscriptionRequestManager.subscribe(subscriptionInfo -> {
      subscriptionService.subscribe(subscriptionInfo.getSubscriberId(),
              subscriptionInfo.getChannels(),
              subscriptionInfo.getCallbackUrl(),
              Optional.of(subscriptionInfo.getSubscriptionInstanceId()),
              subscriptionInfo.isFollower());
    }, subscriptionInfo -> subscriptionService.unsubscribe(subscriptionInfo.getSubscriptionInstanceId()));
  }
}
