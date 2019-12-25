package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.common.json.mapper.JSonMapper;
import org.apache.curator.framework.CuratorFramework;

import java.nio.charset.StandardCharsets;
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
              .forPath(makeSubscriptionPath(subscriptionInfo.getSubscriptionInstanceId()), serializeSubscriptionInfo(subscriptionInfo));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void deleteSubscriptionInfo(String subscriptionInstanceId) {
    try {
      curatorFramework
              .delete()
              .forPath(makeSubscriptionPath(subscriptionInstanceId));
    } catch (Exception e) {
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
                  return deserializeSubscriptionInfo(curatorFramework.getData().forPath(makeSubscriptionPath(subscriptionInstanceId)));
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

  private byte[] serializeSubscriptionInfo(SubscriptionInfo subscriptionInfo) {
    return JSonMapper.toJson(subscriptionInfo).getBytes(StandardCharsets.UTF_8);
  }

  private SubscriptionInfo deserializeSubscriptionInfo(byte[] raw) {
    return JSonMapper.fromJson(new String(raw, StandardCharsets.UTF_8), SubscriptionInfo.class);
  }
}
