package io.eventuate.tram.messaging.proxy.consumer;

public class TestEventInfo {
  private TestEvent testEvent;
  private String aggregateId;
  private String eventId;

  public TestEventInfo(TestEvent testEvent, String aggregateId, String eventId) {
    this.testEvent = testEvent;
    this.aggregateId = aggregateId;
    this.eventId = eventId;
  }

  public TestEvent getTestEvent() {
    return testEvent;
  }

  public String getAggregateId() {
    return aggregateId;
  }

  public String getEventId() {
    return eventId;
  }
}
