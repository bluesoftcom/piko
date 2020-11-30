package com.bluesoft.piko.maps.location.messaging;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface LocationEventsBindings {

    String IN = "location-events-in";

    @Input(IN)
    SubscribableChannel inboundChannel();

}
