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
@SpringBootTest(classes = PropertyTest.Config.class)
public class PropertyTest {
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

    assertEquals("1", subscription1.getSubscriberId());
    assertEquals("orders,order-history", subscription1.getChannels());
    assertEquals("subscription1", subscription1.getCallbackSubscriptionId());

    SubscriptionData subscription2 = eventuateSubscriptionProperties.getMessage().get("2");

    assertEquals("2", subscription2.getSubscriberId());
    assertEquals("customers,customer-history", subscription2.getChannels());
    assertEquals("subscription2", subscription2.getCallbackSubscriptionId());

    SubscriptionData subscription3 = eventuateSubscriptionProperties.getMessage().get("3");

    assertEquals("3", subscription3.getSubscriberId());
    assertEquals("test-channel", subscription3.getChannels());
    assertEquals("subscription3", subscription3.getCallbackSubscriptionId());
  }
}
