package io.eventuate.tram.messaging.proxy.consumer;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.CommandReplyOutcome;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.consumer.http.common.EventuateHttpHeaders;
import io.eventuate.tram.consumer.http.common.HttpMessage;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.http.spring.consumer.duplicatedetection.IdempotentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@RestController
public class TestController {

  @Autowired
  private MessageProducer messageProducer;

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
  @IdempotentHandler
  public void handleMessages(@RequestBody HttpMessage httpMessage) {
    receivedMessages.add(httpMessage);
  }

  @PostMapping(path = "/events/s4/TestAggregate/{aggregateId}/io.eventuate.tram.messaging.proxy.consumer.TestEvent/{eventId}")
  @IdempotentHandler
  public void handleEvent(@PathVariable String aggregateId, @PathVariable String eventId, @RequestBody TestEvent testEvent) {
    receivedEvents.add(new TestEventInfo(testEvent, aggregateId, eventId));
  }

  @PostMapping(path = "/commands/d1/{messageId}/io.eventuate.tram.messaging.proxy.consumer.TestCommand/{replyChannel}/test-resource/{value}")
  @IdempotentHandler
  public void handleCommand(@PathVariable String messageId,
                            @PathVariable String replyChannel,
                            @PathVariable String value,
                            @RequestHeader(EventuateHttpHeaders.COMMAND_REPLY_HEADERS) String headers,
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
    Message message = MessageBuilder
            .withPayload(JSonMapper.toJson(reply))
            .withHeader(ReplyMessageHeaders.REPLY_OUTCOME, outcome.name())
            .withHeader(ReplyMessageHeaders.REPLY_TYPE, reply.getClass().getName())
            .withExtraHeaders("", JSonMapper.fromJson(replyHeaders, Map.class))
            .build();

    messageProducer.send(replyChannel, message);
  }
}
