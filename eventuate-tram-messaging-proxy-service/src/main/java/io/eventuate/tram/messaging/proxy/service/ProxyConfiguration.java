package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.common.jdbc.spring.EventuateCommonJdbcOperationsConfiguration;
import io.eventuate.tram.consumer.jdbc.spring.TramConsumerJdbcAutoConfiguration;
import io.eventuate.tram.consumer.kafka.spring.EventuateTramKafkaMessageConsumerConfiguration;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.DefaultChannelMapping;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
@Import({TramConsumerJdbcAutoConfiguration.class, EventuateTramKafkaMessageConsumerConfiguration.class, EventuateCommonJdbcOperationsConfiguration.class})
public class ProxyConfiguration {

  @Bean
  public HttpMessageConverters customConverters() {
    HttpMessageConverter<?> additional = new MappingJackson2HttpMessageConverter();
    return new HttpMessageConverters(additional);
  }

  @Bean
  public ChannelMapping channelMapping() {
    return new DefaultChannelMapping.DefaultChannelMappingBuilder().build();
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}