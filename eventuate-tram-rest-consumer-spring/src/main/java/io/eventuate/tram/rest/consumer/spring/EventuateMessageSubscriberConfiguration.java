package io.eventuate.tram.rest.consumer.spring;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(EventuateSubscriptionProperties.class)
public class EventuateMessageSubscriberConfiguration {

  @Bean
  public EventuateTramHttpMessageSubscriptionInitializer eventuateTramRestMessageSubscriptionInitializer(EventuateTramHttpMessageSubscriber eventuateTramHttpMessageSubscriber,
                                                                                                         EventuateSubscriptionProperties eventuateSubscriptionProperties) {
    return new EventuateTramHttpMessageSubscriptionInitializer(eventuateSubscriptionProperties, eventuateTramHttpMessageSubscriber);
  }

  @Bean
  public EventuateTramHttpMessageSubscriber eventuateTramRestMessageSubscriber(CircuitBreaker circuitBreaker,
                                                                               Retry retry,
                                                                               ProxyClient proxyClient,
                                                                               HeartbeatService heartbeatService,
                                                                               EventuateHttpConsumerProperties httpConsumerProperties) {

    return new EventuateTramHttpMessageSubscriber(circuitBreaker,
            retry, proxyClient, heartbeatService, httpConsumerProperties.getMessageConsumerBaseUrl());
  }

  @Bean
  public HeartbeatService heartbeatService(CircuitBreaker circuitBreaker,
                                           ProxyClient proxyClient,
                                           EventuateHttpConsumerProperties httpConsumerProperties) {
    return new HeartbeatService(circuitBreaker, proxyClient, httpConsumerProperties);
  }

  @Bean
  public ProxyClient proxyClient(RestTemplate restTemplate, EventuateHttpConsumerProperties httpConsumerProperties) {
    return new ProxyClient(restTemplate, httpConsumerProperties.getHttpProxyBaseUrl());
  }

  @Bean
  public Retry retry(EventuateHttpConsumerProperties httpConsumerProperties) {
    RetryConfig retryConfig = RetryConfig
            .custom()
            .waitDuration(Duration.ofMillis(httpConsumerProperties.getRetryRequestTimeout()))
            .maxAttempts(httpConsumerProperties.getRetryRequests())
            .build();

    return Retry.of("RestSubscriberRetry", retryConfig);
  }

  @Bean
  public CircuitBreaker circuitBreaker(EventuateHttpConsumerProperties httpConsumerProperties) {
    CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig
            .custom()
            .minimumNumberOfCalls(httpConsumerProperties.getRequestCircuitBreakerCalls())
            .waitDurationInOpenState(Duration.ofMillis(httpConsumerProperties.getRequestCircuitBreakerTimeout()))
            .build();

    return CircuitBreaker.of("RestSubscriberCircuitBreaker", circuitBreakerConfig);
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public EventuateHttpConsumerProperties eventuateHttpConsumerProperties() {
    return new EventuateHttpConsumerProperties();
  }
}
