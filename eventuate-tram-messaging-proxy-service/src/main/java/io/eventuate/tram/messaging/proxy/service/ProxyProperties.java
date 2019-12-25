package io.eventuate.tram.messaging.proxy.service;

import org.springframework.beans.factory.annotation.Value;

public class ProxyProperties {

  @Value("${eventuate.http.proxy.max.heartbeat.interval:#{30000}}")
  private int maxHeartbeatInterval;

  @Value("${eventuate.http.proxy.zookeeper.connection.string}")
  private String zookeeperConnectionString;

  @Value("${eventuate.http.proxy.id}")
  private String proxyId;

  public int getMaxHeartbeatInterval() {
    return maxHeartbeatInterval;
  }

  public String getZookeeperConnectionString() {
    return zookeeperConnectionString;
  }

  public String getProxyId() {
    return proxyId;
  }
}
