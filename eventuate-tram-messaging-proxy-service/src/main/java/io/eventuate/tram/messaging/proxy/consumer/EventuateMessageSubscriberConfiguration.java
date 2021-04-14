package io.eventuate.tram.messaging.proxy.consumer;

import io.eventuate.tram.messaging.proxy.service.ProxyConfiguration;
import io.eventuate.tram.messaging.proxy.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(EventuateSubscriptionProperties.class)
@Import(ProxyConfiguration.class)
public class EventuateMessageSubscriberConfiguration {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Bean
  public EventuateTramHttpMessageSubscriptionInitializer eventuateTramRestMessageSubscriptionInitializer(SubscriptionService subscriptionService,
                                                                                                         EventuateSubscriptionProperties eventuateSubscriptionProperties) {
    logger.info("Creating EventuateTramHttpMessageSubscriptionInitializer bean");

    EventuateTramHttpMessageSubscriptionInitializer eventuateTramHttpMessageSubscriptionInitializer =
            new EventuateTramHttpMessageSubscriptionInitializer(eventuateSubscriptionProperties, subscriptionService);

    logger.info("Created EventuateTramHttpMessageSubscriptionInitializer bean");

    return eventuateTramHttpMessageSubscriptionInitializer;
  }
}
