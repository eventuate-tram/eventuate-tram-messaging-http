package io.eventuate.tram.consumer.http;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;

import javax.inject.Singleton;

@Factory
public class EventuateTramHttpMessageConsumerFactory {
  @Singleton
  public MessageConsumerImplementation messageConsumerImplementation(ProxyClient proxyClient,
                                                                     HeartbeatService heartbeatService,
                                                                     EventuateTramHttpMessageController eventuateTramHttpMessageController,
                                                                     HttpConsumerProperties httpConsumerProperties) {
    return new EventuateTramHttpMessageConsumer(proxyClient, heartbeatService, eventuateTramHttpMessageController, httpConsumerProperties.getHttpConsumerBaseUrl());
  }
}