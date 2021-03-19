package io.eventuate.tram.messaging.proxy.consumer;

public class TestReply {
  private String someImportantData;

  public TestReply() {
  }

  public TestReply(String someImportantData) {
    this.someImportantData = someImportantData;
  }

  public String getSomeImportantData() {
    return someImportantData;
  }

  public void setSomeImportantData(String someImportantData) {
    this.someImportantData = someImportantData;
  }
}
