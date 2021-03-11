package io.eventuate.tram.messaging.proxy.consumer;

import io.eventuate.tram.commands.common.Command;

public class TestCommand implements Command {
  private String someImportantData;

  public TestCommand() {
  }

  public TestCommand(String someImportantData) {
    this.someImportantData = someImportantData;
  }

  public String getSomeImportantData() {
    return someImportantData;
  }

  public void setSomeImportantData(String someImportantData) {
    this.someImportantData = someImportantData;
  }
}
