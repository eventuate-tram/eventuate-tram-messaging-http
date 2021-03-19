package io.eventuate.tram.http.spring.consumer.duplicatedetection;

import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.http.common.EventuateHttpHeaders;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
public class IdempotentHandlerAspect {
  private TransactionTemplate transactionTemplate;
  private DuplicateMessageDetector duplicateMessageDetector;

  public IdempotentHandlerAspect(TransactionTemplate transactionTemplate, DuplicateMessageDetector duplicateMessageDetector) {
    this.transactionTemplate = transactionTemplate;
    this.duplicateMessageDetector = duplicateMessageDetector;
  }

  @Around("@annotation(io.eventuate.tram.http.spring.consumer.duplicatedetection.IdempotentHandler)")
  public Object check(ProceedingJoinPoint proceedingJoinPoint) {
    return transactionTemplate.execute(status -> {
      try {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String subscriberId = request.getHeader(EventuateHttpHeaders.SUBSCRIBER_ID);
        String messageId = request.getHeader(EventuateHttpHeaders.MESSAGE_ID);

        if (messageId == null || !duplicateMessageDetector.isDuplicate(subscriberId, messageId)) {
          return proceedingJoinPoint.proceed();
        }

        return null;
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    });
  }
}
