package io.eventuate.tram.consumer.http;

import io.micronaut.context.annotation.Value;

import javax.annotation.Nullable;

public class HttpConsumerProperties {

  @Nullable
  @Value("${eventuate.http.consumer.heartbeat.interval}")
  private int heartBeatInterval = 5000;

  @Value("${eventuate.http.consumer.base.url}")
  private String httpConsumerBaseUrl;

  public int getHeartBeatInterval() {
    return heartBeatInterval;
  }

  public String getHttpConsumerBaseUrl() {
    return httpConsumerBaseUrl;
  }
}
