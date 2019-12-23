package io.eventuate.tram.consumer.http;

import java.util.function.Supplier;

public class CircuitBreaker {
  private State state = State.CLOSED;

  private int maxErrors;
  private int errors;
  private int timeout;
  private long openTime;

  public CircuitBreaker(int maxErrors, int timeout) {
    this.maxErrors = maxErrors;
    this.timeout = timeout;
  }

  public <T> T send(Supplier<T> request) {
    switch (state) {
      case OPEN: {
        if (System.currentTimeMillis() - openTime < timeout) {
          throw new CircuitBreakerException();
        }

        state = State.HALF_OPEN;
      }
      case HALF_OPEN: {
        try {
          state = State.CLOSED;
          return request.get();
        } catch (Throwable t) {
          state = State.OPEN;
          openTime = System.currentTimeMillis();
          throw new CircuitBreakerException(t);
        }
      }
      case CLOSED: {
        try {
          return request.get();
        } catch (Throwable t) {
          if (++errors > maxErrors) {
            errors = 0;
            state = State.OPEN;
            openTime = System.currentTimeMillis();
          }

          throw new CircuitBreakerException(t);
        }
      }

      default: throw new IllegalStateException();// should not be here
    }
  }

  public static class CircuitBreakerException extends RuntimeException {
    public CircuitBreakerException() {
      super("State is 'CLOSED'");
    }

    public CircuitBreakerException(Throwable cause) {
      super(cause);
    }
  }

  private enum State {OPEN, HALF_OPEN, CLOSED}
}
