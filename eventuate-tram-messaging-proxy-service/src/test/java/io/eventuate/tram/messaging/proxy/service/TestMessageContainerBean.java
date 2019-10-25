package io.eventuate.tram.messaging.proxy.service;

import java.util.ArrayList;
import java.util.List;

public class TestMessageContainerBean {
  private List<MessageResponse> messages = new ArrayList<>();

  public List<MessageResponse> getMessages() {
    return messages;
  }
}
