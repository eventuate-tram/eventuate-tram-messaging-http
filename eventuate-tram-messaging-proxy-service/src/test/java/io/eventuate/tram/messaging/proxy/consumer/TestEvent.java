package io.eventuate.tram.messaging.proxy.consumer;

import io.eventuate.tram.events.common.DomainEvent;

public class TestEvent implements DomainEvent {
  private String someImportantData;

  public TestEvent() {
  }

  public TestEvent(String someImportantData) {
    this.someImportantData = someImportantData;
  }

  public String getSomeImportantData() {
    return someImportantData;
  }

  public void setSomeImportantData(String someImportantData) {
    this.someImportantData = someImportantData;
  }
}
