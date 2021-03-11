package io.eventuate.tram.messaging.proxy.consumer;

import io.eventuate.tram.consumer.http.common.HttpMessage;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@RestController
public class TestController {
  private BlockingQueue<HttpMessage> receivedMessages = new LinkedBlockingDeque<>();
  private BlockingQueue<TestEventInfo> receivedEvents = new LinkedBlockingDeque<>();

  public BlockingQueue<HttpMessage> getReceivedMessages() {
    return receivedMessages;
  }

  public BlockingQueue<TestEventInfo> getReceivedEvents() {
    return receivedEvents;
  }

  @PostMapping(path = "/messages/s3")
  public void handleMessages(@RequestBody HttpMessage httpMessage) {
    receivedMessages.add(httpMessage);
  }

  @PostMapping(path = "/events/s4/TestAggregate/{aggregateId}/io.eventuate.tram.messaging.proxy.consumer.TestEvent/{eventId}")
  public void handleEvent(@PathVariable String aggregateId, @PathVariable String eventId, @RequestBody TestEvent testEvent) {
    receivedEvents.add(new TestEventInfo(testEvent, aggregateId, eventId));
  }
}
