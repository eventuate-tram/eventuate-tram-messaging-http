package io.eventuate.tram.messaging.proxy.service;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZkConfig {
  @Bean
  public ProxyProperties proxyProperties() {
    return new ProxyProperties();
  }

  @Bean
  public CuratorFramework curatorFramework(ProxyProperties proxyProperties) {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(100, Integer.MAX_VALUE);

    CuratorFramework curatorFramework = CuratorFrameworkFactory.
            builder().retryPolicy(retryPolicy)
            .connectString(proxyProperties.getZookeeperConnectionString())
            .build();

    curatorFramework.start();

    return curatorFramework;
  }
}
