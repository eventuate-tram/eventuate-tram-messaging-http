package io.eventuate.tram.messaging.proxy.service;

import org.springframework.beans.factory.annotation.Value;

public class ProxyProperties {

  @Value("${eventuate.http.proxy.zookeeper.connection.string}")
  private String zookeeperConnectionString;

  @Value("${eventuate.http.proxy.id}")
  private String proxyId;

  @Value("${eventuate.subscription.request.ttl:#{120000}}")
  private int subscriptionRequestTtl;

  public String getZookeeperConnectionString() {
    return zookeeperConnectionString;
  }

  public String getProxyId() {
    return proxyId;
  }

  public int getSubscriptionRequestTtl() {
    return subscriptionRequestTtl;
  }
}
