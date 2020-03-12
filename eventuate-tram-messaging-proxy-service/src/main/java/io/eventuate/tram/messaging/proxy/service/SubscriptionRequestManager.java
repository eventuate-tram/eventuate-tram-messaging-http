package io.eventuate.tram.messaging.proxy.service;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class SubscriptionRequestManager {
  private String path;

  private Logger logger = LoggerFactory.getLogger(getClass());
  private CuratorFramework curatorFramework;
  private TreeCache treeCache;
  private int ttl;

  public SubscriptionRequestManager(CuratorFramework curatorFramework, String path, int ttl) {

    this.path = path;
    this.curatorFramework = curatorFramework;
    this.ttl = ttl;
  }

  public void subscribe(Consumer<SubscriptionInfo> nodeAddedCallback, Consumer<SubscriptionInfo> nodeRemovedCallback) {
    if (treeCache != null) {
      treeCache.close();
    }

    treeCache = new TreeCache(curatorFramework, path);

    treeCache.getListenable().addListener((client, event) -> {
      Consumer<SubscriptionInfo> callback;

      switch (event.getType()) {
        case NODE_ADDED: {
          callback = nodeAddedCallback;
          break;
        }
        case NODE_REMOVED: {
          callback = nodeRemovedCallback;
          break;
        }
        default: return;
      }

      if (event.getData().getData() != null && event.getData().getData().length != 0) {
        callback.accept(SubscriptionUtils.deserializeSubscriptionInfo(event.getData().getData()));
      }
    });

    try {
      treeCache.start();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  public void touch(String subscriptionInstanceId) {
    String path = pathForSubscriptionRequest(subscriptionInstanceId);

    try {
      curatorFramework
              .create()
              .orSetData()
              .withTtl(ttl)
              .creatingParentContainersIfNeeded()
              .withMode(CreateMode.PERSISTENT_WITH_TTL)
              .forPath(path, curatorFramework.getData().forPath(path));
    }
    catch (KeeperException.NoNodeException e) {
      //ignore
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void createSubscriptionRequest(SubscriptionInfo subscriptionInfo) {
    try {
      curatorFramework
              .create()
              .orSetData()
              .withTtl(ttl)
              .creatingParentContainersIfNeeded()
              .withMode(CreateMode.PERSISTENT_WITH_TTL)
              .forPath(pathForSubscriptionRequest(subscriptionInfo.getSubscriptionInstanceId()), SubscriptionUtils.serializeSubscriptionInfo(subscriptionInfo));

    }
    catch (KeeperException.NodeExistsException e) {
      //ignore
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void removeSubscriptionRequest(String subscriptionInstanceId) {
    try {
      curatorFramework
              .delete()
              .forPath(pathForSubscriptionRequest(subscriptionInstanceId));
    }
    catch (KeeperException.NoNodeException e) {
      //ignore
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    treeCache.close();
  }

  public String pathForSubscriptionRequest(String subscriptionInstanceId) {
    return String.format("%s/%s", path, subscriptionInstanceId);
  }
}
