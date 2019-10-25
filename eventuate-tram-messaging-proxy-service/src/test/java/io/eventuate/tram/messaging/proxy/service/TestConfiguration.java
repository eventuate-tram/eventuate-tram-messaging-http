package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.tram.messaging.producer.jdbc.spring.TramMessageProducerJdbcConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@Import({ProxyConfiguration.class, TramMessageProducerJdbcConfiguration.class})
public class TestConfiguration {
  @Bean
  public TestMessageContainerBean testMessageContainerBean() {
    return new TestMessageContainerBean();
  }
}
