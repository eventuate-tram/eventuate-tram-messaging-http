package io.eventuate.tram.messaging.proxy.consumer;

import java.util.Map;

public class TestCommandInfo {
  private Map<String, String> headers;
  private TestCommand testCommand;
  private String messageId;
  private String replyChannel;
  private String value;

  public TestCommandInfo(Map<String, String> headers, TestCommand testCommand, String messageId, String replyChannel, String value) {
    this.headers = headers;
    this.testCommand = testCommand;
    this.messageId = messageId;
    this.replyChannel = replyChannel;
    this.value = value;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public TestCommand getTestCommand() {
    return testCommand;
  }

  public String getMessageId() {
    return messageId;
  }

  public String getReplyChannel() {
    return replyChannel;
  }

  public String getValue() {
    return value;
  }
}
