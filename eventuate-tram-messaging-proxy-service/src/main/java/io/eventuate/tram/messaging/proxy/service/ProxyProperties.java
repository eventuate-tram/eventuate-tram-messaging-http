package io.eventuate.tram.messaging.proxy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProxyProperties {

  @Value("${eventuate.http.proxy.max.heartbeat.interval:#{30000}}")
  private int maxHeartbeatInterval;

  public int getMaxHeartbeatInterval() {
    return maxHeartbeatInterval;
  }
}
