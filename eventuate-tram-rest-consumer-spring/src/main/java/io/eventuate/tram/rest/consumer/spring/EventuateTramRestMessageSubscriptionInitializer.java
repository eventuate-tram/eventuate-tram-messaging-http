package io.eventuate.tram.rest.consumer.spring;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.stream.Collectors;

public class EventuateTramRestMessageSubscriptionInitializer {
  private EventuateSubscriptionProperties eventuateSubscriptionProperties;
  private EventuateTramRestMessageSubscriber eventuateTramRestMessageSubscriber;

  public EventuateTramRestMessageSubscriptionInitializer(EventuateSubscriptionProperties eventuateSubscriptionProperties,
                                                         EventuateTramRestMessageSubscriber eventuateTramRestMessageSubscriber) {

    this.eventuateSubscriptionProperties = eventuateSubscriptionProperties;
    this.eventuateTramRestMessageSubscriber = eventuateTramRestMessageSubscriber;
  }

  @PostConstruct
  public void subscribe() {
    eventuateSubscriptionProperties.getMessage().keySet().forEach(subscription -> {
      SubscriptionData subscriptionData = eventuateSubscriptionProperties.getMessage().get(subscription);

      eventuateTramRestMessageSubscriber
              .subscribe(subscriptionData.getSubscriberId(),
                      subscriptionData.getCallbackSubscriptionId(),
                      Arrays.stream(subscriptionData.getChannels().split(",")).collect(Collectors.toSet()));
    });
  }

  @PreDestroy
  public void unsubscribe() {
    eventuateTramRestMessageSubscriber.close();
  }
}
