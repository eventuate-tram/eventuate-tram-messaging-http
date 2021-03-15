package io.eventuate.tram.messaging.proxy.consumer.duplicatedetection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CheckDuplicatePublishing {
  //subscriber id
  String value();
}
