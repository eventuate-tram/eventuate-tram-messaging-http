package io.eventuate.tram.messaging.proxy.consumer.duplicatedetection;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.http.common.HttpMessage;
import io.eventuate.tram.events.common.DomainEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
public class DuplicatePublishingAspect {
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private DuplicateMessageDetector duplicateMessageDetector;

  @Around("@annotation(io.eventuate.tram.messaging.proxy.consumer.duplicatedetection.CheckDuplicatePublishing)")
  public Object check(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

    Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();

    String subscriberId = method.getAnnotation(CheckDuplicatePublishing.class).value();

    return transactionTemplate.execute(status -> {
      try {
        String messageId = getMessageId(subscriberId, method, proceedingJoinPoint);

        if (messageId == null || !duplicateMessageDetector.isDuplicate(subscriberId, messageId)) {
          return proceedingJoinPoint.proceed();
        }

        return null;
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    });
  }

  private String getMessageId(String subscriberId, Method method, ProceedingJoinPoint proceedingJoinPoint) {
    if (isMessageHandler(method)) {
      return Arrays
              .stream(proceedingJoinPoint.getArgs())
              .filter(o -> HttpMessage.class.isAssignableFrom(o.getClass()))
              .map(o -> (HttpMessage) o)
              .findAny()
              .get()
              .getId();
    } else {
      String path = method.getAnnotation(PostMapping.class).path()[0];

      String messageIdDefinition = null;

      if (isEventHandler(method)) {
        String[] parts = path.split("/");
        messageIdDefinition = parts[parts.length - 1];
      }

      if (isCommandHandler(method)) {
        String subscriberIdEntry = String.format("/%s/", subscriberId);

        messageIdDefinition =
                path.substring(path.indexOf(subscriberIdEntry) + subscriberIdEntry.length()).split("/")[0];
      }

      if (messageIdDefinition == null) {
        throw new RuntimeException("TODO"); //TODO
      }

      String messageIdVariableName = messageIdDefinition.substring(1, messageIdDefinition.length() - 1);

      Annotation[][] parameterAnnotations = method.getParameterAnnotations();

      for (int i = 0; i < parameterAnnotations.length; i++) {
        for (int j = 0; j < parameterAnnotations[i].length; j++) {
          Annotation annotation = parameterAnnotations[i][j];

          if (annotation.annotationType().equals(PathVariable.class)) {
            PathVariable pathVariable = (PathVariable)annotation;

            if (messageIdVariableName.equals(pathVariable.value())) {
              return pathVariable.value();
            }
          }
        }
      }

      for (int i = 0; i < method.getParameterCount(); i++) {
        if (method.getParameters()[i].getName().equals(messageIdVariableName)) {
          return proceedingJoinPoint.getArgs()[i].toString();
        }
      }
    }

    throw new RuntimeException("TODO"); // TODO
  }

  private boolean isMessageHandler(Method method) {
    return checkMethodParameterExists(method, HttpMessage.class);
  }

  private boolean isEventHandler(Method method) {
    return checkMethodParameterExists(method, DomainEvent.class);
  }

  private boolean isCommandHandler(Method method) {
    return checkMethodParameterExists(method, Command.class);
  }

  private boolean checkMethodParameterExists(Method method, Class<?> type) {
    return Arrays
            .stream(method.getParameterTypes())
            .anyMatch(type::isAssignableFrom);
  }
}
