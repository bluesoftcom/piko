package com.bluesoft.piko.admin.location.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Objects;

@Component
@AllArgsConstructor
public class LocationEventsMessageCollector {

    private final LocationEventsBindings locationEventsBindings;
    private final ObjectMapper objectMapper;
    private final MessageCollector messageCollector;

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public LocationMessage captureLastLocationMessage() {
        final Message<String> lastMessage = (Message<String>) messageCollector
                .forChannel(locationEventsBindings.outboundChannel())
                .poll();
        Objects.requireNonNull(lastMessage, "Could not find last message for: " + locationEventsBindings.OUT + " binding");
        return objectMapper.readValue(lastMessage.getPayload(), LocationMessage.class);
    }

    public void dumpMessages() {
        messageCollector.forChannel(locationEventsBindings.outboundChannel()).drainTo(new ArrayList<>());
    }

}
