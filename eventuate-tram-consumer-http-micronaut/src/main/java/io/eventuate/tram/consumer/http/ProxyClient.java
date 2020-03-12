package io.eventuate.tram.consumer.http;

import io.eventuate.tram.consumer.http.common.SubscribeRequest;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;

@Client("${eventuate.http.proxy.base.url}")
public interface ProxyClient {

  @Post("/")
  String subscribe(@Body SubscribeRequest subscribeRequest);

  @Post("/{subscriptionInstanceId}/heartbeat")
  void heartbeat(String subscriptionInstanceId);

  @Delete("/{subscriptionInstanceId}")
  void unsubscribe(String subscriptionInstanceId);
}
