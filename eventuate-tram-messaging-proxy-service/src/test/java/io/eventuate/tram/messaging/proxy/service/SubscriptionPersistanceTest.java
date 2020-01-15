package io.eventuate.tram.messaging.proxy.service;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ZkConfig.class)
public class SubscriptionPersistanceTest {
  @Autowired
  private CuratorFramework curatorFramework;

  private SubscriptionPersistenceService subscriptionPersistenceService;

  @Before
  public void init() {
    subscriptionPersistenceService = new SubscriptionPersistenceService(curatorFramework, "/" + generateId());
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
