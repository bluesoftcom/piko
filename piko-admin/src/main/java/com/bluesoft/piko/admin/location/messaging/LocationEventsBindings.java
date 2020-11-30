package com.bluesoft.piko.admin.location.messaging;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface LocationEventsBindings {

    String OUT = "location-events-out";

    String IN = "location-events-in";

    @Output(OUT)
    MessageChannel outboundChannel();

    @Input(IN)
    SubscribableChannel inboundChannel();

}
