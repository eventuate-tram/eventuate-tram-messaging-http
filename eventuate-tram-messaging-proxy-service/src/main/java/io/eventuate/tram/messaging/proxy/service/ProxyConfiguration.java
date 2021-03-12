package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.common.spring.jdbc.EventuateCommonJdbcOperationsConfiguration;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.DefaultChannelMapping;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.spring.consumer.jdbc.TramConsumerJdbcAutoConfiguration;
import io.eventuate.tram.spring.consumer.kafka.EventuateTramKafkaMessageConsumerConfiguration;
import io.eventuate.tram.spring.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
@Import({TramConsumerJdbcAutoConfiguration.class,
        EventuateTramKafkaMessageConsumerConfiguration.class,
        EventuateCommonJdbcOperationsConfiguration.class,
        TramMessageProducerJdbcConfiguration.class})
public class ProxyConfiguration {

  @Bean
  public ProxyProperties proxyProperties() {
    return new ProxyProperties();
  }

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

  @Bean
  public CuratorFramework curatorFramework(ProxyProperties proxyProperties) {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(100, Integer.MAX_VALUE);

    CuratorFramework curatorFramework = CuratorFrameworkFactory.
            builder().retryPolicy(retryPolicy)
            .connectString(proxyProperties.getZookeeperConnectionString())
            .build();

    curatorFramework.start();

    return curatorFramework;
  }

  @Bean
  public SubscriptionService subscriptionService(SubscriptionPersistenceService subscriptionPersistenceService,
                                                 SubscriptionRequestManager subscriptionRequestManager,
                                                 RestTemplate restTemplate,
                                                 MessageConsumerImplementation messageConsumerImplementation,
                                                 MessageProducer messageProducer) {

    return new SubscriptionService(subscriptionPersistenceService,
            subscriptionRequestManager,
            restTemplate,
            new SynchronizedMessageConsumerImplementation(messageConsumerImplementation),
            messageProducer);
  }

  @Bean
  public SubscriptionPersistenceService subscriptionPersistenceService(CuratorFramework curatorFramework) {

    return new SubscriptionPersistenceService(curatorFramework, "/eventuate/proxy/persistent/subscriptions");
  }

  @Bean
  public SubscriptionRequestManager subscriptionRequestManager(CuratorFramework curatorFramework, ProxyProperties proxyProperties) {
    return new SubscriptionRequestManager(curatorFramework, "/eventuate/proxy/cluster/subscriptions", proxyProperties.getSubscriptionRequestTtl());
  }

  @Bean
  public SubscriptionLoader subscriptionLoader(SubscriptionService subscriptionService,
                                               SubscriptionPersistenceService subscriptionPersistenceService,
                                               SubscriptionRequestManager subscriptionRequestManager) {
    return new SubscriptionLoader(subscriptionService, subscriptionPersistenceService, subscriptionRequestManager);
  }
}