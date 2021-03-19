package io.eventuate.tram.messaging.proxy.consumer;

import io.eventuate.tram.commands.common.CommandReplyOutcome;

public class TestReplyInfo {
  private String replyToCommandId;
  private String replyType;
  private CommandReplyOutcome outcome;
  private String resourceValue;
  private TestReply reply;

  public TestReplyInfo() {
  }

  public TestReplyInfo(String replyToCommandId, String replyType, CommandReplyOutcome outcome, String resourceValue, TestReply reply) {
    this.replyToCommandId = replyToCommandId;
    this.replyType = replyType;
    this.outcome = outcome;
    this.resourceValue = resourceValue;
    this.reply = reply;
  }

  public String getReplyToCommandId() {
    return replyToCommandId;
  }

  public void setReplyToCommandId(String replyToCommandId) {
    this.replyToCommandId = replyToCommandId;
  }

  public String getReplyType() {
    return replyType;
  }

  public void setReplyType(String replyType) {
    this.replyType = replyType;
  }

  public CommandReplyOutcome getOutcome() {
    return outcome;
  }

  public void setOutcome(CommandReplyOutcome outcome) {
    this.outcome = outcome;
  }

  public String getResourceValue() {
    return resourceValue;
  }

  public void setResourceValue(String resourceValue) {
    this.resourceValue = resourceValue;
  }

  public TestReply getReply() {
    return reply;
  }

  public void setReply(TestReply reply) {
    this.reply = reply;
  }
}
