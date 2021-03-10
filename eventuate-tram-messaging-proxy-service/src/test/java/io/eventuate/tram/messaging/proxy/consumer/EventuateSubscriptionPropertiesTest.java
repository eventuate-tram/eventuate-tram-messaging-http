package io.eventuate.tram.messaging.proxy.consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EventuateSubscriptionPropertiesTest.Config.class)
public class EventuateSubscriptionPropertiesTest {
  @Configuration
  @EnableConfigurationProperties(EventuateSubscriptionProperties.class)
  public static class Config {
  }

  @Autowired
  private EventuateSubscriptionProperties eventuateSubscriptionProperties;

  @Test
  public void testProperties() {
    assertEquals(3, eventuateSubscriptionProperties.getMessage().size());

    MessageSubscriptionData messageSubscription1 = eventuateSubscriptionProperties.getMessage().get("1");

    assertEquals("orders,order-history", messageSubscription1.getChannels());
    assertTrue(messageSubscription1.getUrl().endsWith("messages/1"));

    MessageSubscriptionData messageSubscription2 = eventuateSubscriptionProperties.getMessage().get("2");

    assertEquals("customers,customer-history", messageSubscription2.getChannels());
    assertTrue(messageSubscription2.getUrl().endsWith("messages/2"));

    MessageSubscriptionData messageSubscription3 = eventuateSubscriptionProperties.getMessage().get("3");

    assertEquals("test-channel", messageSubscription3.getChannels());
    assertTrue(messageSubscription3.getUrl().endsWith("messages/3"));

    EventSubscriptionData eventSubscription4 = eventuateSubscriptionProperties.getEvent().get("4");

    assertEquals("TestAggregate", eventSubscription4.getAggregate());
    assertEquals("io.eventuate.tram.messaging.proxy.consumer.TestEvent", eventSubscription4.getEvents());
    assertTrue(eventSubscription4.getBaseUrl().endsWith("/events/4"));
  }
}
