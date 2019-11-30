package io.eventuate.tram.consumer.http;

import io.eventuate.tram.consumer.http.common.SubscribeRequest;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;

@Client("${eventuate.http.proxy.base.url}")
public interface ProxyClient {

  @Post("/")
  void subscribe(@Body SubscribeRequest subscribeRequest);

  @Delete("/{subscriberId}")
  void unsubscribe(String subscriberId);
}
