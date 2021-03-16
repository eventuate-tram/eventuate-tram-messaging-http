package io.eventuate.tram.http.spring.consumer.duplicatedetection;

import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class IdempotentHandlerConfiguration {
  @Bean
  public IdempotentHandlerAspect duplicatePublishingAspect(TransactionTemplate transactionTemplate,
                                                           DuplicateMessageDetector duplicateMessageDetector) {
    return new IdempotentHandlerAspect(transactionTemplate, duplicateMessageDetector);
  }
}
