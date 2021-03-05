package io.eventuate.tram.rest.consumer.spring;

import io.eventuate.tram.consumer.http.common.SubscribeRequest;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;

import java.util.HashSet;
import java.util.Set;

public class EventuateTramRestMessageSubscriber {
  private CircuitBreaker circuitBreaker;
  private Retry retry;
  private String httpConsumerBaseUrl;
  private ProxyClient proxyClient;
  private HeartbeatService heartbeatService;
  private Set<String> subscriptions = new HashSet<>();

  public EventuateTramRestMessageSubscriber(CircuitBreaker circuitBreaker,
                                            Retry retry,
                                            ProxyClient proxyClient,
                                            HeartbeatService heartbeatService,
                                            String httpConsumerBaseUrl) {

    this.retry = retry;
    this.circuitBreaker = circuitBreaker;
    this.proxyClient = proxyClient;
    this.heartbeatService = heartbeatService;
    this.httpConsumerBaseUrl = httpConsumerBaseUrl;
  }

  public Runnable subscribe(String subscriberId, String callSubscriptionId, Set<String> channels) {
    String subscriptionInstanceId = retry.executeSupplier(() ->
            circuitBreaker.executeSupplier(() ->
                    proxyClient.subscribe(new SubscribeRequest(callSubscriptionId, subscriberId, channels, httpConsumerBaseUrl))));

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
