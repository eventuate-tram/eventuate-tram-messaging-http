package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.util.test.async.Eventually;
import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootTest(classes = ZkConfig.class)
public class SubscriptionRequestManagerTest {
  @Autowired
  private CuratorFramework curatorFramework;

  private SubscriptionInfo subscriptionInfo;
  private AtomicReference<SubscriptionInfo> addedSubscription;
  private AtomicReference<SubscriptionInfo> removedSubscription;
  private SubscriptionRequestManager subscriptionRequestManager;

  @BeforeEach
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
      Assertions.assertNull(removedSubscription.get());
      Thread.sleep(500);
    }

    timer.cancel();
  }

  private void assertSubscriptionAdded() {
    Eventually.eventually(() -> {
      Assertions.assertNull(removedSubscription.get());
      Assertions.assertEquals(subscriptionInfo, addedSubscription.get());
    });
  }

  private void assertSubscriptionRemoved() {
    assertSubscriptionRemoved(180);
  }

  private void assertSubscriptionRemoved(int iterations) {
    Eventually.eventually(iterations, 500, TimeUnit.MILLISECONDS, () ->
            Assertions.assertEquals(subscriptionInfo, removedSubscription.get()));
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
