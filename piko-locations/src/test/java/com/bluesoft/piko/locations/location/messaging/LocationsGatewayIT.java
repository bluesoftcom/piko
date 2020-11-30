package com.bluesoft.piko.locations.location.messaging;

import com.bluesoft.piko.locations.PikoLocationsIT;
import com.bluesoft.piko.locations.location.Location;
import com.bluesoft.piko.locations.location.LocationsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static com.bluesoft.piko.locations.location.LocationFixtures.someLocation;
import static org.assertj.core.api.Assertions.assertThat;

class LocationsGatewayIT extends PikoLocationsIT {

    @Autowired
    private LocationsGateway locationsGateway;
    @Autowired
    private LocationsRepository locationsRepository;
    @Autowired
    private LocationEventsMessageCollector locationEventsMessageCollector;

    @BeforeEach
    void setUp() {
        locationEventsMessageCollector.dumpMessages();
    }

    @Test
    void testSendPublicationRequestWithdrawn() {
        // given:
        final Location location = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .build()
        );

        // when:
        locationsGateway.sendPublicationRequestWithdrawn(location.getId());

        // then:
        final LocationMessage message = locationEventsMessageCollector.captureLastLocationMessage();
        assertThat(message).isEqualTo(
                LocationMessage.builder()
                        .id(location.getId())
                        .status(location.getStatus())
                        .event(LocationEventType.PUBLICATION_REQUEST_WITHDRAWN)
                        .lat(location.getLat())
                        .lng(location.getLng())
                        .createdAt(location.getCreatedAt())
                        .owner(location.getOwner())
                        .name(location.getName())
                        .build()
        );
    }

    @Test
    void testSendPublicationRequestIssued() {
        // given:
        final Location location = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .build()
        );

        // when:
        locationsGateway.sendPublicationRequestIssued(location.getId());

        // then:
        final LocationMessage message = locationEventsMessageCollector.captureLastLocationMessage();
        assertThat(message).isEqualTo(
                LocationMessage.builder()
                        .id(location.getId())
                        .status(location.getStatus())
                        .event(LocationEventType.PUBLICATION_REQUEST_ISSUED)
                        .lat(location.getLat())
                        .lng(location.getLng())
                        .createdAt(location.getCreatedAt())
                        .owner(location.getOwner())
                        .name(location.getName())
                        .build()
        );
    }

}