package io.eventuate.tram.rest.consumer.spring;

import io.eventuate.tram.consumer.http.common.SubscribeRequest;
import io.eventuate.tram.consumer.http.common.SubscriptionType;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;

import java.util.HashSet;
import java.util.Set;

public class EventuateTramHttpMessageSubscriber {
  private CircuitBreaker circuitBreaker;
  private Retry retry;
  private ProxyClient proxyClient;
  private HeartbeatService heartbeatService;
  private Set<String> subscriptions = new HashSet<>();

  public EventuateTramHttpMessageSubscriber(CircuitBreaker circuitBreaker,
                                            Retry retry,
                                            ProxyClient proxyClient,
                                            HeartbeatService heartbeatService) {

    this.retry = retry;
    this.circuitBreaker = circuitBreaker;
    this.proxyClient = proxyClient;
    this.heartbeatService = heartbeatService;
  }

  public Runnable subscribe(SubscriptionType subscriptionType, String subscriberId, Set<String> channels, String callbackUrl) {
    String subscriptionInstanceId = retry.executeSupplier(() ->
            circuitBreaker.executeSupplier(() ->
                    proxyClient.subscribe(new SubscribeRequest(subscriptionType, subscriberId, channels, callbackUrl, true))));

    heartbeatService.addSubscription(subscriptionInstanceId);

    subscriptions.add(subscriptionInstanceId);

    return () -> {
      subscriptions.remove(subscriptionInstanceId);
      unsubscribe(subscriptionInstanceId);
    };
  }

  public void close() {
    subscriptions.forEach(this::unsubscribe);
  }

  private void unsubscribe(String subscriptionInstanceId) {
    heartbeatService.removeSubscription(subscriptionInstanceId);
    retry.executeRunnable(() -> circuitBreaker.executeRunnable(() -> proxyClient.unsubscribe(subscriptionInstanceId)));
  }
}
