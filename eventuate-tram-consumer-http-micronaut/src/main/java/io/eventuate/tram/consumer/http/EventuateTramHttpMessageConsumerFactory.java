package io.eventuate.tram.consumer.http;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;

import javax.annotation.Nullable;
import javax.inject.Singleton;

@Factory
public class EventuateTramHttpMessageConsumerFactory {
  @Singleton
  public MessageConsumerImplementation messageConsumerImplementation(EventuateTramHttpMessageController eventuateTramHttpMessageController,
                                                                     @Value("${eventuate.http.proxy.base.url}") String httpProxyBaseUrl,
                                                                     @Value("${eventuate.http.consumer.base.url}") String httpConsumerBaseUrl) {
    return new EventuateTramHttpMessageConsumer(eventuateTramHttpMessageController, httpProxyBaseUrl, httpConsumerBaseUrl);
  }
}