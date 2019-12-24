package io.eventuate.tram.consumer.http;

import io.eventuate.util.test.async.Eventually;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.UUID;

public class HeartbeatTest {

  private CircuitBreaker circuitBreaker;
  private Retry retry;
  private ProxyClient proxyClient;
  private HeartbeatService heartbeatService;

  @Test
  public void testWorkingClient() {
    createCircuitBreaker(Integer.MAX_VALUE);
    createRetry(Integer.MAX_VALUE);
    createProxyClient();
    createAndStartHeartbeatService();
    assertHeartbeats(1);
  }

  @Test
  public void testRetry() {
    int retries = 3;

    createCircuitBreaker(Integer.MAX_VALUE);
    createRetry(retries);
    createProxyClient();
    makeHeartbeatFail();
    createAndStartHeartbeatService();
    assertHeartbeats(retries);
  }

  @Test
  public void testCircuitBreakerBlock() {
    int attempts = 3;

    createCircuitBreaker(attempts);
    createRetry(10);
    createProxyClient();
    makeHeartbeatFail();
    createAndStartHeartbeatService();
    assertHeartbeats(attempts);
  }

  private void assertHeartbeats(int count) {
    Eventually.eventually(() -> Mockito.verify(proxyClient, Mockito.times(count)).heartbeat(Mockito.any()));
  }

  private void makeHeartbeatFail() {
    Mockito.doThrow(new RuntimeException()).when(proxyClient).heartbeat(Mockito.any());
  }

  private void createAndStartHeartbeatService() {
    HttpConsumerProperties httpConsumerProperties = new HttpConsumerProperties();
    httpConsumerProperties.setHeartBeatInterval(Integer.MAX_VALUE);

    heartbeatService = new HeartbeatService(circuitBreaker, retry, proxyClient, httpConsumerProperties);
    heartbeatService.addSubscription(generateId());
    heartbeatService.start();
  }

  private void createProxyClient() {
    proxyClient = Mockito.mock(ProxyClient.class);
  }

  private void createCircuitBreaker(int calls) {
    circuitBreaker = CircuitBreaker.of(generateId(),
            CircuitBreakerConfig
                    .custom()
                    .waitDurationInOpenState(Duration.ofMillis(Integer.MAX_VALUE))
                    .minimumNumberOfCalls(calls)
                    .build());
  }

  private void createRetry(int attempts) {
    retry = Retry.of(generateId(),
            RetryConfig
                    .custom()
                    .maxAttempts(attempts)
                    .waitDuration(Duration.ofMillis(0))
                    .build());
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
