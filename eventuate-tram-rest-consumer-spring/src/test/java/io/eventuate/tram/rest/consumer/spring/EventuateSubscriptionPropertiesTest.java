package io.eventuate.tram.rest.consumer.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

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

    SubscriptionData subscription1 = eventuateSubscriptionProperties.getMessage().get("1");

    assertEquals("orders,order-history", subscription1.getChannels());

    SubscriptionData subscription2 = eventuateSubscriptionProperties.getMessage().get("2");

    assertEquals("customers,customer-history", subscription2.getChannels());

    SubscriptionData subscription3 = eventuateSubscriptionProperties.getMessage().get("3");

    assertEquals("test-channel", subscription3.getChannels());
  }
}
