package io.eventuate.tram.rest.consumer.spring;

import io.eventuate.tram.consumer.http.common.HttpMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@RestController
public class TestController {
  private BlockingQueue<HttpMessage> receivedMessages = new LinkedBlockingDeque<>();

  public BlockingQueue<HttpMessage> getReceivedMessages() {
    return receivedMessages;
  }

  @PostMapping(path = "/messages/subscription3")
  public void handleMessages(@RequestBody HttpMessage httpMessage) {
    receivedMessages.add(httpMessage);
  }
}
