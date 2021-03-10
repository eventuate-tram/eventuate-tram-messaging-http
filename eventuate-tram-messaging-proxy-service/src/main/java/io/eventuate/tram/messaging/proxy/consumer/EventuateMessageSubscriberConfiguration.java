package io.eventuate.tram.messaging.proxy.consumer;

import io.eventuate.tram.messaging.proxy.service.ProxyConfiguration;
import io.eventuate.tram.messaging.proxy.service.SubscriptionService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(EventuateSubscriptionProperties.class)
@Import(ProxyConfiguration.class)
public class EventuateMessageSubscriberConfiguration {

  @Bean
  public EventuateTramHttpMessageSubscriptionInitializer eventuateTramRestMessageSubscriptionInitializer(SubscriptionService subscriptionService,
                                                                                                         EventuateSubscriptionProperties eventuateSubscriptionProperties) {
    return new EventuateTramHttpMessageSubscriptionInitializer(eventuateSubscriptionProperties, subscriptionService);
  }
}
