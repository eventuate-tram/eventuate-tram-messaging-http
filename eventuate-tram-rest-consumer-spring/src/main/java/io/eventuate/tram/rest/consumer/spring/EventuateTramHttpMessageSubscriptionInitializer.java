package io.eventuate.tram.rest.consumer.spring;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.stream.Collectors;

public class EventuateTramHttpMessageSubscriptionInitializer {
  private EventuateSubscriptionProperties eventuateSubscriptionProperties;
  private EventuateTramHttpMessageSubscriber eventuateTramHttpMessageSubscriber;

  public EventuateTramHttpMessageSubscriptionInitializer(EventuateSubscriptionProperties eventuateSubscriptionProperties,
                                                         EventuateTramHttpMessageSubscriber eventuateTramHttpMessageSubscriber) {

    this.eventuateSubscriptionProperties = eventuateSubscriptionProperties;
    this.eventuateTramHttpMessageSubscriber = eventuateTramHttpMessageSubscriber;
  }

  @PostConstruct
  public void subscribe() {
    eventuateSubscriptionProperties.getMessage().keySet().forEach(subscriberId -> {
      SubscriptionData subscriptionData = eventuateSubscriptionProperties.getMessage().get(subscriberId);

      eventuateTramHttpMessageSubscriber
              .subscribe(subscriberId,
                      Arrays.stream(subscriptionData.getChannels().split(",")).collect(Collectors.toSet()));
    });
  }

  @PreDestroy
  public void unsubscribe() {
    eventuateTramHttpMessageSubscriber.close();
  }
}
