package io.eventuate.tram.messaging.proxy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestMessageController {

  @Autowired
  private TestMessageContainerBean testMessageContainerBean;

  @RequestMapping(value = "/messages", method = RequestMethod.POST)
  public void handleMessage(@RequestBody MessageResponse messageResponse) {
    testMessageContainerBean.getMessages().add(messageResponse);
  }
}
