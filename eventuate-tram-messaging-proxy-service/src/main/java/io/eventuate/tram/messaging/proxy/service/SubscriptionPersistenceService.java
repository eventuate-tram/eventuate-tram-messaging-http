package io.eventuate.tram.messaging.proxy.service;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;

import java.util.Set;
import java.util.stream.Collectors;

public class SubscriptionPersistenceService {
  private CuratorFramework curatorFramework;
  private String proxyId;

  public SubscriptionPersistenceService(CuratorFramework curatorFramework, String proxyId) {
    this.curatorFramework = curatorFramework;
    this.proxyId = proxyId;
  }

  public void saveSubscriptionInfo(SubscriptionInfo subscriptionInfo) {
    try {
      curatorFramework
              .create()
              .creatingParentContainersIfNeeded()
              .forPath(makeSubscriptionPath(subscriptionInfo.getSubscriptionInstanceId()),
                      SubscriptionUtils.serializeSubscriptionInfo(subscriptionInfo));
    } catch (KeeperException.NodeExistsException e) {
      //ignore
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void deleteSubscriptionInfo(String subscriptionInstanceId) {
    try {
      curatorFramework
              .delete()
              .forPath(makeSubscriptionPath(subscriptionInstanceId));
    }
    catch (KeeperException.NoNodeException e) {
      //ignore
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Set<SubscriptionInfo> loadSubscriptionInfos() {
    try {
      curatorFramework.createContainers(makeRootPath());

      return curatorFramework
              .getChildren()
              .forPath(makeRootPath())
              .stream()
              .map(subscriptionInstanceId -> {
                try {
                  return SubscriptionUtils.deserializeSubscriptionInfo(curatorFramework
                          .getData()
                          .forPath(makeSubscriptionPath(subscriptionInstanceId)));
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }
              })
              .collect(Collectors.toSet());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String makeSubscriptionPath(String subscriptionInstanceId) {
    return String.format("%s/%s",  makeRootPath(), subscriptionInstanceId);
  }

  private String makeRootPath() {
    return String.format("/eventuate/proxy/%s/subscriptions", proxyId);
  }
}
