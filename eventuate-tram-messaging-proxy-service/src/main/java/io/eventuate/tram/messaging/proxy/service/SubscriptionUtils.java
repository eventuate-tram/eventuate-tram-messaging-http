package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.common.json.mapper.JSonMapper;

import java.nio.charset.StandardCharsets;

public class SubscriptionUtils {
  public static byte[] serializeSubscriptionInfo(SubscriptionInfo subscriptionInfo) {
    return JSonMapper.toJson(subscriptionInfo).getBytes(StandardCharsets.UTF_8);
  }

  public static SubscriptionInfo deserializeSubscriptionInfo(byte[] raw) {
    return JSonMapper.fromJson(new String(raw, StandardCharsets.UTF_8), SubscriptionInfo.class);
  }
}
