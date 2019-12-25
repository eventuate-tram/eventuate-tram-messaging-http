package io.eventuate.tram.messaging.proxy.service;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SubscriptionPersistanceTest.Config.class)
public class SubscriptionPersistanceTest {

  @Configuration
  public static class Config {
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

  @Autowired
  private CuratorFramework curatorFramework;

  private SubscriptionPersistenceService subscriptionPersistenceService;

  @Before
  public void init() {
    subscriptionPersistenceService = new SubscriptionPersistenceService(curatorFramework, generateId());
  }

  @Test
  public void testSaveLoad() {
    Set<SubscriptionInfo> subscriptionInfos = generateSubscriptionInfos();
    subscriptionInfos.forEach(subscriptionPersistenceService::saveSubscriptionInfo);
    Set<SubscriptionInfo> loadedSubscriptionInfos = subscriptionPersistenceService.loadSubscriptionInfos();
    Assert.assertEquals(subscriptionInfos, loadedSubscriptionInfos);
  }

  @Test
  public void testDelete() {
    Set<SubscriptionInfo> subscriptionInfos = generateSubscriptionInfos();
    subscriptionInfos.forEach(subscriptionPersistenceService::saveSubscriptionInfo);

    SubscriptionInfo subscriptionInfoToDelete = subscriptionInfos.stream().findAny().get();
    subscriptionInfos.remove(subscriptionInfoToDelete);
    subscriptionPersistenceService.deleteSubscriptionInfo(subscriptionInfoToDelete.getSubscriptionInstanceId());

    Set<SubscriptionInfo>loadedSubscriptionInfos = subscriptionPersistenceService.loadSubscriptionInfos();

    Assert.assertEquals(subscriptionInfos, loadedSubscriptionInfos);
  }

  private Set<SubscriptionInfo> generateSubscriptionInfos() {
    Set<SubscriptionInfo> subscriptionInfos = new HashSet<>();

    for (int i = 0; i < 10; i++) {
      subscriptionInfos.add(new SubscriptionInfo(generateId(),
              generateId(),
              Collections.singleton(generateId()),
              generateId()));
    }

    return subscriptionInfos;
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
