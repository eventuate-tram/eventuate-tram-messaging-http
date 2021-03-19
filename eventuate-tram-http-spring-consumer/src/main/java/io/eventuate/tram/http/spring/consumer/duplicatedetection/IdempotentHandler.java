package io.eventuate.tram.http.spring.consumer.duplicatedetection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface IdempotentHandler {
}
