package com.bluesoft.piko.locations.location.messaging;

import com.bluesoft.piko.locations.location.Location;
import com.bluesoft.piko.locations.location.LocationsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;

@Component
@AllArgsConstructor
@Slf4j
public class LocationsGateway {

    private final LocationsMessagingGateway locationsMessagingGateway;
    private final TransactionOperations transactionOperations;
    private final LocationsRepository locationsRepository;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public void sendPublicationRequestWithdrawn(String locationId) {
        logSendingLocationEvent(locationId, LocationEventType.PUBLICATION_REQUEST_WITHDRAWN);

        final LocationMessage locationMessage = transactionOperations.execute(status -> {
            final Location location = locationsRepository.find(locationId);
            return toLocationMessage(location, LocationEventType.PUBLICATION_REQUEST_WITHDRAWN);
        });

        final String json = objectMapper.writeValueAsString(locationMessage);
        locationsMessagingGateway.send(new GenericMessage<>(json));
    }

    @SneakyThrows
    public void sendPublicationRequestIssued(String locationId) {
        logSendingLocationEvent(locationId, LocationEventType.PUBLICATION_REQUEST_ISSUED);

        final LocationMessage locationMessage = transactionOperations.execute(status -> {
            final Location location = locationsRepository.find(locationId);
            return toLocationMessage(location, LocationEventType.PUBLICATION_REQUEST_ISSUED);
        });

        final String json = objectMapper.writeValueAsString(locationMessage);
        locationsMessagingGateway.send(new GenericMessage<>(json));
    }

    private static LocationMessage toLocationMessage(Location location, LocationEventType event) {
        return LocationMessage.builder()
                .id(location.getId())
                .event(event)
                .status(location.getStatus())
                .name(location.getName())
                .lat(location.getLat())
                .lng(location.getLng())
                .createdAt(location.getCreatedAt())
                .owner(location.getOwner())
                .build();
    }

    private static void logSendingLocationEvent(String locationId, LocationEventType event) {
        log.info("Sending event: {} message for location: {}", event, locationId);
    }
}
