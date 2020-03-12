package io.eventuate.tram.consumer.http;

import io.eventuate.util.test.async.Eventually;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.UUID;

public class HeartbeatTest {

  private CircuitBreaker circuitBreaker;
  private ProxyClient proxyClient;
  private HeartbeatService heartbeatService;

  @Test
  public void testWorkingClient() {
    createCircuitBreaker(Integer.MAX_VALUE);
    createProxyClient();
    createAndStartHeartbeatService(Integer.MAX_VALUE);
    assertHeartbeats(1);
  }

  @Test
  public void testCircuitBreakerBlock() throws InterruptedException {
    int attempts = 3;

    createCircuitBreaker(attempts);
    createProxyClient();
    makeHeartbeatFail();
    createAndStartHeartbeatService(1);
    Thread.sleep(1000);
    assertHeartbeats(attempts);
  }

  private void assertHeartbeats(int count) {
    Eventually.eventually(() -> Mockito.verify(proxyClient, Mockito.times(count)).heartbeat(Mockito.any()));
  }

  private void makeHeartbeatFail() {
    Mockito.doThrow(new RuntimeException()).when(proxyClient).heartbeat(Mockito.any());
  }

  private void createAndStartHeartbeatService(int interval) {
    HttpConsumerProperties httpConsumerProperties = new HttpConsumerProperties();
    httpConsumerProperties.setHeartBeatInterval(interval);

    heartbeatService = new HeartbeatService(circuitBreaker, proxyClient, httpConsumerProperties);
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

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
