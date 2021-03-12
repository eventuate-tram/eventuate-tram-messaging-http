package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.CommandReplyOutcome;
import io.eventuate.tram.consumer.http.common.SubscribeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class SubscriptionController {

  @Autowired
  private SubscriptionService subscriptionService;

  @RequestMapping(value = "/subscriptions", method = RequestMethod.POST)
  public String subscribe(@RequestBody SubscribeRequest subscribeRequest) {
    return subscriptionService.makeSubscriptionRequest(subscribeRequest.getSubscriberId(),
            subscribeRequest.getChannels(), subscribeRequest.getCallbackUrl());
  }

  @RequestMapping(value = "/subscriptions/reply/{replyChannel}/{replyType}/{outcome}", method = RequestMethod.POST)
  public void reply(@PathVariable String replyChannel,
                    @PathVariable String replyType,
                    @PathVariable CommandReplyOutcome outcome,
                    @RequestHeader("EVENTUATE_COMMAND_REPLY_HEADERS") String headers,
                    @RequestBody String reply) {


    subscriptionService.sendReply(reply, outcome, replyType, JSonMapper.fromJson(headers, Map.class), replyChannel);
  }

  @RequestMapping(value = "/subscriptions/{subscriptionInstanceId}/heartbeat", method = RequestMethod.POST)
  public void heartbeat(@PathVariable(name = "subscriptionInstanceId") String subscriptionInstanceId) {
    subscriptionService.updateSubscription(subscriptionInstanceId);
  }

  @RequestMapping(value = "/subscriptions/{subscriptionInstanceId}", method = RequestMethod.DELETE)
  public void unsubscribe(@PathVariable(name = "subscriptionInstanceId") String subscriptionInstanceId) {
    subscriptionService.makeUnsubscriptionRequest(subscriptionInstanceId);
  }
}
