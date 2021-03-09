package io.eventuate.tram.rest.consumer.spring;

import io.eventuate.tram.consumer.http.common.SubscriptionType;

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
      MessageSubscriptionData messageSubscriptionData = eventuateSubscriptionProperties.getMessage().get(subscriberId);

      subscribe(SubscriptionType.MESSAGE,
              messageSubscriptionData.getUrl(),
              subscriberId,
              messageSubscriptionData.getChannels());
    });

    eventuateSubscriptionProperties.getEvent().keySet().forEach(subscriberId -> {
      EventSubscriptionData eventSubscriptionData = eventuateSubscriptionProperties.getEvent().get(subscriberId);

      subscribe(SubscriptionType.EVENT,
              eventSubscriptionData.getUrl(),
              subscriberId,
              eventSubscriptionData.getAggregates());
    });
  }

  private void subscribe(SubscriptionType subscriptionType, String callbackUrl, String subscriberId, String channels) {
    eventuateTramHttpMessageSubscriber
            .subscribe(subscriptionType,
                    subscriberId, Arrays.stream(channels.split(",")).collect(Collectors.toSet()), callbackUrl);
  }

  @PreDestroy
  public void unsubscribe() {
    eventuateTramHttpMessageSubscriber.close();
  }
}
