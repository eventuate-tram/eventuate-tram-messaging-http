package io.eventuate.tram.rest.consumer.spring;

import io.eventuate.tram.events.common.DomainEvent;

public class TestEvent implements DomainEvent {
  private String id;
  private String someImportantData;

  public TestEvent() {
  }

  public TestEvent(String id, String someImportantData) {
    this.id = id;
    this.someImportantData = someImportantData;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSomeImportantData() {
    return someImportantData;
  }

  public void setSomeImportantData(String someImportantData) {
    this.someImportantData = someImportantData;
  }
}
