package io.eventuate.tram.consumer.http;

import io.micronaut.context.annotation.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

@Context
public class HeartbeatService {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private ProxyClient proxyClient;
  private int interval;
  private Timer timer;
  private Set<String> subscriptionInstanceIds = new CopyOnWriteArraySet<>();
  private CircuitBreaker circuitBreaker = new CircuitBreaker(5, 5000); //will be moved to props

  public HeartbeatService(ProxyClient proxyClient, HttpConsumerProperties httpConsumerProperties) {
    this.proxyClient = proxyClient;
    this.interval = httpConsumerProperties.getHeartBeatInterval();
  }

  public void addSubscription(String subscriptionInstanceId) {
    subscriptionInstanceIds.add(subscriptionInstanceId);
  }

  public void removeSubscription(String subscriptionInstanceId) {
    subscriptionInstanceIds.remove(subscriptionInstanceId);
  }

  @PostConstruct
  public void start() {
    timer = new Timer();

    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        subscriptionInstanceIds.forEach(id -> {
          try {
            circuitBreaker.send(() -> {
              proxyClient.heartbeat(id);
              return null;
            });
          } catch (CircuitBreaker.CircuitBreakerException e) {
            logger.error("Heartbeat failed", e);
          }
        });
      }
    }, interval);
  }
}
