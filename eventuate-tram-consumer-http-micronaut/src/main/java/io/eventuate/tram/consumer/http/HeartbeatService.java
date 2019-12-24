package io.eventuate.tram.consumer.http;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micronaut.context.annotation.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@Context
public class HeartbeatService {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private CircuitBreaker circuitBreaker;
  private Retry retry;
  private ProxyClient proxyClient;
  private int interval;
  private Timer timer = new Timer();
  private ConcurrentHashMap<String, Runnable> heartbeatCalls = new ConcurrentHashMap<>();

  public HeartbeatService(CircuitBreaker circuitBreaker,
                          Retry retry,
                          ProxyClient proxyClient,
                          HttpConsumerProperties httpConsumerProperties) {
    this.circuitBreaker = circuitBreaker;
    this.retry = retry;
    this.proxyClient = proxyClient;
    this.interval = httpConsumerProperties.getHeartBeatInterval();
  }

  public void addSubscription(String subscriptionInstanceId) {
    //circuitBreaker is one for all subscriptions, if we have 5 subscriptions, and circuit breaker calls is limited by 3
    //if heartbeat fails for first 3 subscription/retry calls, other heartbeats/retries will be blocked
    //so we can wrap each heartbeat individually
    heartbeatCalls.put(subscriptionInstanceId,
            Retry.decorateRunnable(retry,
                    circuitBreaker.decorateRunnable(
                            () -> proxyClient.heartbeat(subscriptionInstanceId))));
  }

  public void removeSubscription(String subscriptionInstanceId) {
    heartbeatCalls.remove(subscriptionInstanceId);
  }

  @PostConstruct
  public void start() {
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        heartbeatCalls.values().forEach(Runnable::run);
      }
    }, 0, interval);
  }
}
