package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.util.test.async.Eventually;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ZkConfig.class)
public class SubscriptionRequestManagerTest {
  @Autowired
  private CuratorFramework curatorFramework;

  private SubscriptionInfo subscriptionInfo;
  private AtomicReference<SubscriptionInfo> addedSubscription;
  private AtomicReference<SubscriptionInfo> removedSubscription;
  private SubscriptionRequestManager subscriptionRequestManager;

  @Before
  public void init() {
    subscriptionInfo = new SubscriptionInfo(generateId(), generateId(), Collections.singleton(generateId()), generateId());

    addedSubscription = new AtomicReference<>();
    removedSubscription = new AtomicReference<>();

    subscriptionRequestManager = new SubscriptionRequestManager(curatorFramework,
            "/" + generateId(),
            10000);

    subscriptionRequestManager.subscribe(addedSubscription::set, removedSubscription::set);

    subscriptionRequestManager.createSubscriptionRequest(subscriptionInfo);
  }

  @Test
  public void TestAddingAndExpiring() {
    assertSubscriptionAdded();
    assertSubscriptionRemoved();
  }

  @Test
  public void TestTouch() throws InterruptedException {
    assertSubscriptionAdded();
    assertSubscriptionPersistedWhileTouching();
    assertSubscriptionRemoved();
  }

  @Test
  public void testRemoving() {
    assertSubscriptionAdded();
    subscriptionRequestManager.removeSubscriptionRequest(subscriptionInfo.getSubscriptionInstanceId());
    assertSubscriptionRemoved(2);
  }

  private void assertSubscriptionPersistedWhileTouching() throws InterruptedException {
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        subscriptionRequestManager.touch(subscriptionInfo.getSubscriptionInstanceId());
      }
    }, 0, 1000);

    for (int i = 0; i < 180; i++) {
      Assert.assertNull(removedSubscription.get());
      Thread.sleep(500);
    }

    timer.cancel();
  }

  private void assertSubscriptionAdded() {
    Eventually.eventually(() -> {
      Assert.assertNull(removedSubscription.get());
      Assert.assertEquals(subscriptionInfo, addedSubscription.get());
    });
  }

  private void assertSubscriptionRemoved() {
    assertSubscriptionRemoved(180);
  }

  private void assertSubscriptionRemoved(int iterations) {
    Eventually.eventually(iterations, 500, TimeUnit.MILLISECONDS, () ->
            Assert.assertEquals(subscriptionInfo, removedSubscription.get()));
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
