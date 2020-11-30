package com.bluesoft.piko.admin.location.messaging;

import com.bluesoft.piko.admin.location.AdminLocationsService;
import com.bluesoft.piko.admin.location.json.DetailedJsonLocation;
import com.bluesoft.piko.admin.location.json.JsonLocation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class LocationEventsListener {

    private final ObjectMapper objectMapper;
    private final AdminLocationsService adminLocationsService;

    @StreamListener(LocationEventsBindings.IN)
    public void onLocationEventReceived(Message<String> message) throws Exception {
        log.debug("Received location-events message: {}", message);

        final LocationMessage locationMessage = objectMapper.readValue(message.getPayload(), LocationMessage.class);
        final DetailedJsonLocation location = toDetailedJsonLocation(locationMessage);
        switch (locationMessage.getEvent()) {
            case PUBLICATION_REQUEST_ISSUED:
                adminLocationsService.requestPublicationReview(location);
                break;
            case PUBLICATION_REQUEST_WITHDRAWN:
                adminLocationsService.cancelPublicationReview(location.getId());
                break;
            default:
                log.debug("Ignoring location event: {} for location: {}", locationMessage.getEvent(), location.getId());
        }
    }

    private static DetailedJsonLocation toDetailedJsonLocation(LocationMessage locationMessage) {
        return DetailedJsonLocation.builder()
                .id(locationMessage.getId())
                .createdAt(locationMessage.getCreatedAt())
                .status(locationMessage.getStatus())
                .owner(locationMessage.getOwner())
                .location(
                        JsonLocation.builder()
                                .lat(locationMessage.getLat())
                                .lng(locationMessage.getLng())
                                .name(locationMessage.getName())
                                .build()
                )
                .build();
    }

}
