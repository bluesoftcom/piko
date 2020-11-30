package com.bluesoft.piko.admin.location.messaging;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.Message;

@MessagingGateway
interface LocationsMessagingGateway {

    @Gateway(requestChannel = LocationEventsBindings.OUT)
    void send(Message<String> message);

}
