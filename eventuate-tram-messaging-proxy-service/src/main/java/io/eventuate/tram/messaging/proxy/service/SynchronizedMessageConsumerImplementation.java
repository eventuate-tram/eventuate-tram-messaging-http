package io.eventuate.tram.messaging.proxy.service;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;

import java.util.Set;

public class SynchronizedMessageConsumerImplementation implements MessageConsumerImplementation {

  private MessageConsumerImplementation messageConsumerImplementation;

  public SynchronizedMessageConsumerImplementation(MessageConsumerImplementation messageConsumerImplementation) {
    this.messageConsumerImplementation = messageConsumerImplementation;
  }

  @Override
  public synchronized MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    return messageConsumerImplementation.subscribe(subscriberId, channels, handler);
  }

  @Override
  public String getId() {
    return messageConsumerImplementation.getId();
  }

  @Override
  public synchronized void close() {
    messageConsumerImplementation.close();
  }
}
