package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.util.test.async.Eventually;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ZkConfig.class)
public class SubscriptionRequestManagerTest {
  @Autowired
  private CuratorFramework curatorFramework;

  @Test
  public void TestAddAndRemove() {
    SubscriptionInfo subscriptionInfo = new SubscriptionInfo(generateId(), generateId(), Collections.singleton(generateId()), generateId());

    AtomicReference<SubscriptionInfo> addedSubscription = new AtomicReference<>();
    AtomicReference<SubscriptionInfo> removedSubscription = new AtomicReference<>();

    SubscriptionRequestManager subscriptionRequestManager = new SubscriptionRequestManager(curatorFramework,
            "/" + generateId(),
            3000,
            addedSubscription::set,
            removedSubscription::set);

    subscriptionRequestManager.createSubscriptionRequest(subscriptionInfo);

    Eventually.eventually(() -> {
      Assert.assertNull(removedSubscription.get());
      Assert.assertEquals(subscriptionInfo, addedSubscription.get());
    });

    Eventually.eventually(180, 500, TimeUnit.MILLISECONDS, () ->
      Assert.assertEquals(subscriptionInfo, removedSubscription.get()));
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
