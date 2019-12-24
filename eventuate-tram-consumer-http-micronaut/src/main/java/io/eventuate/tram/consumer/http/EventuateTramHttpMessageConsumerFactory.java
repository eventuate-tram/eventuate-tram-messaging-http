package io.eventuate.tram.consumer.http;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;
import java.time.Duration;

@Factory
public class EventuateTramHttpMessageConsumerFactory {
  @Singleton
  public MessageConsumerImplementation messageConsumerImplementation(CircuitBreaker circuitBreaker,
                                                                     Retry retry,
                                                                     ProxyClient proxyClient,
                                                                     HeartbeatService heartbeatService,
                                                                     EventuateTramHttpMessageController eventuateTramHttpMessageController,
                                                                     HttpConsumerProperties httpConsumerProperties) {
    return new EventuateTramHttpMessageConsumer(circuitBreaker, retry, proxyClient, heartbeatService, eventuateTramHttpMessageController, httpConsumerProperties.getHttpConsumerBaseUrl());
  }

  @Singleton
  public CircuitBreaker circuitBreaker(HttpConsumerProperties httpConsumerProperties) {
    CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig
            .custom()
            .minimumNumberOfCalls(httpConsumerProperties.getRequestCircuitBreakerCalls())
            .waitDurationInOpenState(Duration.ofMillis(httpConsumerProperties.getRequestCircuitBreakerTimeout()))
            .build();

    return CircuitBreaker.of("HttpConsumerCircuitBreaker", circuitBreakerConfig);
  }

  @Singleton
  public Retry retry(HttpConsumerProperties httpConsumerProperties) {
    RetryConfig retryConfig = RetryConfig
            .custom()
            .waitDuration(Duration.ofMillis(httpConsumerProperties.getRetryRequestTimeout()))
            .maxAttempts(httpConsumerProperties.getRetryRequests())
            .build();

    return Retry.of("HttpConsumerRetry", retryConfig);
  }
}