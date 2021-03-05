package io.eventuate.tram.rest.consumer.spring;

import io.eventuate.tram.consumer.http.common.SubscribeRequest;
import org.springframework.web.client.RestTemplate;

public class ProxyClient {
  private RestTemplate restTemplate;
  private String url;

  public ProxyClient(RestTemplate restTemplate, String url) {
    this.restTemplate = restTemplate;
    this.url = url;
  }

  public String subscribe(SubscribeRequest subscribeRequest) {
    return restTemplate.postForObject(url, subscribeRequest, String.class);
  }

  public void heartbeat(String subscriptionInstanceId) {
    restTemplate.postForLocation(String.format("%s/%s/%s", url, subscriptionInstanceId, "heartbeat"), null);
  }

  public void unsubscribe(String subscriptionInstanceId) {
    restTemplate.delete(String.format("%s/%s", url, subscriptionInstanceId));
  }
}
