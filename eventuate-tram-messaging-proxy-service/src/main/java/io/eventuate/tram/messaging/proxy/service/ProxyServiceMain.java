package io.eventuate.tram.messaging.proxy.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ProxyConfiguration.class)
public class ProxyServiceMain {
  public static void main(String[] args) {
    SpringApplication.run(ProxyServiceMain.class, args);
  }
}
