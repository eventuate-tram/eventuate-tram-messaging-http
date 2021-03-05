package io.eventuate.tram.rest.consumer.spring;


import org.springframework.beans.factory.annotation.Value;

public class HttpConsumerProperties {
  @Value("${eventuate.http.consumer.heartbeat.interval:#{5000}}")
  private int heartBeatInterval;

  @Value("${eventuate.rest.consumer.base.url}")
  private String restConsumerBaseUrl;

  @Value("${eventuate.http.proxy.base.url}")
  private String httpProxyBaseUrl;

  @Value("${eventuate.http.consumer.request.circuit.breaker.calls:#{3}}")
  private int requestCircuitBreakerCalls;

  @Value("${eventuate.http.consumer.request.circuit.breaker.timeout:#{3000}}")
  private int requestCircuitBreakerTimeout;

  @Value("${eventuate.http.consumer.retry.requests:#{3}}")
  private int retryRequests;

  @Value("${eventuate.http.consumer.retry.request.timeout:#{500}}")
  private int retryRequestTimeout;

  public int getHeartBeatInterval() {
    return heartBeatInterval;
  }

  public String getRestConsumerBaseUrl() {
    return restConsumerBaseUrl;
  }

  public String getHttpProxyBaseUrl() {
    return httpProxyBaseUrl;
  }

  public int getRequestCircuitBreakerCalls() {
    return requestCircuitBreakerCalls;
  }

  public int getRequestCircuitBreakerTimeout() {
    return requestCircuitBreakerTimeout;
  }

  public int getRetryRequests() {
    return retryRequests;
  }

  public int getRetryRequestTimeout() {
    return retryRequestTimeout;
  }
}
