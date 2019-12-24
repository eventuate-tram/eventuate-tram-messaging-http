package io.eventuate.tram.consumer.http;

import io.micronaut.context.annotation.Value;

import javax.annotation.Nullable;

public class HttpConsumerProperties {

  @Nullable
  @Value("${eventuate.http.consumer.heartbeat.interval}")
  private int heartBeatInterval = 5000;

  @Value("${eventuate.http.consumer.base.url}")
  private String httpConsumerBaseUrl;

  @Nullable
  @Value("${eventuate.http.consumer.request.circuit.breaker.calls}")
  private int requestCircuitBreakerCalls = 3;

  @Nullable
  @Value("${eventuate.http.consumer.request.circuit.breaker.timeout}")
  private int requestCircuitBreakerTimeout=3000;

  @Nullable
  @Value("${eventuate.http.consumer.retry.requests}")
  private int retryRequests = 3;

  @Nullable
  @Value("${eventuate.http.consumer.retry.request.timeout}")
  private int retryRequestTimeout = 500;

  public int getHeartBeatInterval() {
    return heartBeatInterval;
  }

  public void setHeartBeatInterval(int heartBeatInterval) {
    this.heartBeatInterval = heartBeatInterval;
  }

  public String getHttpConsumerBaseUrl() {
    return httpConsumerBaseUrl;
  }

  public void setHttpConsumerBaseUrl(String httpConsumerBaseUrl) {
    this.httpConsumerBaseUrl = httpConsumerBaseUrl;
  }

  public int getRequestCircuitBreakerCalls() {
    return requestCircuitBreakerCalls;
  }

  public void setRequestCircuitBreakerCalls(int requestCircuitBreakerCalls) {
    this.requestCircuitBreakerCalls = requestCircuitBreakerCalls;
  }

  public int getRequestCircuitBreakerTimeout() {
    return requestCircuitBreakerTimeout;
  }

  public void setRequestCircuitBreakerTimeout(int requestCircuitBreakerTimeout) {
    this.requestCircuitBreakerTimeout = requestCircuitBreakerTimeout;
  }

  public int getRetryRequests() {
    return retryRequests;
  }

  public void setRetryRequests(int retryRequests) {
    this.retryRequests = retryRequests;
  }

  public int getRetryRequestTimeout() {
    return retryRequestTimeout;
  }

  public void setRetryRequestTimeout(int retryRequestTimeout) {
    this.retryRequestTimeout = retryRequestTimeout;
  }
}
