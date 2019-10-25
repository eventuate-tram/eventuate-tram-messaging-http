package io.eventuate.tram.messaging.proxy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestMessageController {

  @Autowired
  private TestMessageContainerBean testMessageContainerBean;

  @RequestMapping(value = "/messages/{subscriptionId}", method = RequestMethod.POST)
  public void handleMessage(@RequestBody MessageResponse messageResponse,
                            @PathVariable(name = "subscriptionId") String subscriptionId) {

    messageResponse.setSubscriptionId(subscriptionId);
    testMessageContainerBean.getMessages().add(messageResponse);
  }
}
