package io.eventuate.tram.messaging.proxy.consumer;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.CommandReplyOutcome;
import io.eventuate.tram.consumer.http.common.HttpMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@RestController
public class TestController {

  @Value("${eventuate.http.proxy.base.url}")
  private String proxyBaseUrl;

  private RestTemplate restTemplate = new RestTemplate();

  private BlockingQueue<HttpMessage> receivedMessages = new LinkedBlockingDeque<>();
  private BlockingQueue<TestEventInfo> receivedEvents = new LinkedBlockingDeque<>();
  private BlockingQueue<TestCommandInfo> receivedCommands = new LinkedBlockingDeque<>();
  private BlockingQueue<TestReplyInfo> receivedReplies = new LinkedBlockingDeque<>();

  public BlockingQueue<HttpMessage> getReceivedMessages() {
    return receivedMessages;
  }

  public BlockingQueue<TestEventInfo> getReceivedEvents() {
    return receivedEvents;
  }

  public BlockingQueue<TestCommandInfo> getReceivedCommands() {
    return receivedCommands;
  }

  public BlockingQueue<TestReplyInfo> getReceivedReplies() {
    return receivedReplies;
  }

  @PostMapping(path = "/messages/s3")
  public void handleMessages(@RequestBody HttpMessage httpMessage) {
    receivedMessages.add(httpMessage);
  }

  @PostMapping(path = "/events/s4/TestAggregate/{aggregateId}/io.eventuate.tram.messaging.proxy.consumer.TestEvent/{eventId}")
  public void handleEvent(@PathVariable String aggregateId, @PathVariable String eventId, @RequestBody TestEvent testEvent) {
    receivedEvents.add(new TestEventInfo(testEvent, aggregateId, eventId));
  }

  @PostMapping(path = "/commands/d1/{messageId}/io.eventuate.tram.messaging.proxy.consumer.TestCommand/{replyChannel}/test-resource/{value}")
  public void handleCommand(@PathVariable String messageId,
                            @PathVariable String replyChannel,
                            @PathVariable String value,
                            @RequestHeader("EVENTUATE_COMMAND_REPLY_HEADERS") String headers,
                            @RequestBody TestCommand testCommand) {
    receivedCommands.add(new TestCommandInfo(JSonMapper.fromJson(headers, Map.class), testCommand, messageId, replyChannel, value));

    publishReply(new TestReply(String.format("reply to %s", messageId)),
            CommandReplyOutcome.SUCCESS,
            headers,
            replyChannel);
  }

  @PostMapping(path = "/replies/s6/io.eventuate.tram.messaging.proxy.consumer.TestCommand/{replyToCommandId}/{replyType}/{outcome}/test-resource/{value}")
  public void handleReply(@PathVariable String replyToCommandId,
                          @PathVariable String replyType,
                          @PathVariable CommandReplyOutcome outcome,
                          @PathVariable String value,
                          @RequestBody TestReply reply) {

    receivedReplies.add(new TestReplyInfo(replyToCommandId, replyType, outcome, value, reply));
  }

  private void publishReply(Object reply, CommandReplyOutcome outcome, String replyHeaders, String replyChannel) {
    String location = String.format("%s/reply/%s/%s/%s",
            proxyBaseUrl,
            replyChannel,
            reply.getClass().getName(),
            outcome);

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.add("EVENTUATE_COMMAND_REPLY_HEADERS", replyHeaders);
    restTemplate.postForLocation(location, new HttpEntity<>(reply, httpHeaders));
  }
}
