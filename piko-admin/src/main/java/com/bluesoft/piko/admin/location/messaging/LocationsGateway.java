package com.bluesoft.piko.admin.location.messaging;

import com.bluesoft.piko.admin.location.json.DetailedJsonLocation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class LocationsGateway {

    private final LocationsMessagingGateway locationsMessagingGateway;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public void sendLocationRejected(DetailedJsonLocation location) {
        logSendingLocationEvent(location.getId(), LocationEventType.LOCATION_REJECTED);

        final LocationMessage message = toLocationMessage(location, LocationEventType.LOCATION_REJECTED);
        final String json = objectMapper.writeValueAsString(message);
        locationsMessagingGateway.send(new GenericMessage<>(json));
    }

    @SneakyThrows
    public void sendLocationPublished(DetailedJsonLocation location) {
        logSendingLocationEvent(location.getId(), LocationEventType.LOCATION_PUBLISHED);

        final LocationMessage message = toLocationMessage(location, LocationEventType.LOCATION_PUBLISHED);
        final String json = objectMapper.writeValueAsString(message);
        locationsMessagingGateway.send(new GenericMessage<>(json));
    }

    private static LocationMessage toLocationMessage(DetailedJsonLocation location, LocationEventType event) {
        return LocationMessage.builder()
                .id(location.getId())
                .event(event)
                .name(location.getLocation().getName())
                .lat(location.getLocation().getLat())
                .status(location.getStatus())
                .lng(location.getLocation().getLng())
                .createdAt(location.getCreatedAt())
                .owner(location.getOwner())
                .build();
    }

    private static void logSendingLocationEvent(String locationId, LocationEventType event) {
        log.info("Sending event: {} message for location: {}", event, locationId);
    }
}
