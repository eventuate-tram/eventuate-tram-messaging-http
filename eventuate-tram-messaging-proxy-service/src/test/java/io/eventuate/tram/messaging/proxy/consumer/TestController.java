package io.eventuate.tram.messaging.proxy.consumer;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.consumer.http.common.HttpMessage;
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
  private BlockingQueue<HttpMessage> receivedMessages = new LinkedBlockingDeque<>();
  private BlockingQueue<TestEventInfo> receivedEvents = new LinkedBlockingDeque<>();
  private BlockingQueue<TestCommandInfo> receivedCommands = new LinkedBlockingDeque<>();

  public BlockingQueue<HttpMessage> getReceivedMessages() {
    return receivedMessages;
  }

  public BlockingQueue<TestEventInfo> getReceivedEvents() {
    return receivedEvents;
  }

  public BlockingQueue<TestCommandInfo> getReceivedCommands() {
    return receivedCommands;
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
                            @RequestHeader("EVENTUATE_COMMAND_HEADERS") String headers,
                            @RequestBody TestCommand testCommand) {
    receivedCommands.add(new TestCommandInfo(JSonMapper.fromJson(headers, Map.class), testCommand, messageId, replyChannel, value));
  }
}
