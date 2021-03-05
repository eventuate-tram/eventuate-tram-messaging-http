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
public class RestSubscriberConfiguration {

  @Bean
  public EventuateTramRestMessageSubscriptionInitializer eventuateTramRestMessageSubscriptionInitializer(EventuateTramRestMessageSubscriber eventuateTramRestMessageSubscriber,
                                                                                                         EventuateSubscriptionProperties eventuateSubscriptionProperties) {
    return new EventuateTramRestMessageSubscriptionInitializer(eventuateSubscriptionProperties, eventuateTramRestMessageSubscriber);
  }

  @Bean
  public EventuateTramRestMessageSubscriber eventuateTramRestMessageSubscriber(CircuitBreaker circuitBreaker,
                                                                               Retry retry,
                                                                               ProxyClient proxyClient,
                                                                               HeartbeatService heartbeatService,
                                                                               HttpConsumerProperties httpConsumerProperties) {

    return new EventuateTramRestMessageSubscriber(circuitBreaker,
            retry, proxyClient, heartbeatService, httpConsumerProperties.getRestConsumerBaseUrl());
  }

  @Bean
  public HeartbeatService heartbeatService(CircuitBreaker circuitBreaker,
                                           ProxyClient proxyClient,
                                           HttpConsumerProperties httpConsumerProperties) {
    return new HeartbeatService(circuitBreaker, proxyClient, httpConsumerProperties);
  }

  @Bean
  public ProxyClient proxyClient(RestTemplate restTemplate, HttpConsumerProperties httpConsumerProperties) {
    return new ProxyClient(restTemplate, httpConsumerProperties.getHttpProxyBaseUrl());
  }

  @Bean
  public Retry retry(HttpConsumerProperties httpConsumerProperties) {
    RetryConfig retryConfig = RetryConfig
            .custom()
            .waitDuration(Duration.ofMillis(httpConsumerProperties.getRetryRequestTimeout()))
            .maxAttempts(httpConsumerProperties.getRetryRequests())
            .build();

    return Retry.of("RestSubscriberRetry", retryConfig);
  }

  @Bean
  public CircuitBreaker circuitBreaker(HttpConsumerProperties httpConsumerProperties) {
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
  public HttpConsumerProperties httpConsumerProperties() {
    return new HttpConsumerProperties();
  }
}
