package com.bluesoft.piko.locations.location.messaging;

import com.bluesoft.piko.locations.PikoLocationsIT;
import com.bluesoft.piko.locations.location.Location;
import com.bluesoft.piko.locations.location.LocationStatus;
import com.bluesoft.piko.locations.location.LocationsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.support.GenericMessage;

import java.util.UUID;

import static com.bluesoft.piko.locations.location.LocationFixtures.someLocation;
import static org.assertj.core.api.Assertions.assertThat;

class LocationEventsListenerIT extends PikoLocationsIT {

    @Autowired
    private LocationEventsMessageCollector locationEventsMessageCollector;
    @Autowired
    private LocationEventsBindings locationEventsBindings;
    @Autowired
    private MessageCollector messageCollector;
    @Autowired
    private LocationsRepository locationsRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLocationPublishedEvent() throws Exception {
        // given:
        final Location location = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .status(LocationStatus.AWAITING_PUBLICATION)
                        .lastPublishedAt(null)
                        .build()
        );
        final LocationMessage message = toLocationMessage(location, LocationEventType.LOCATION_PUBLISHED);

        // when:
        locationEventsBindings.inboundChannel().send(
                new GenericMessage<>(
                        objectMapper.writeValueAsString(message)
                )
        );

        // then:
        final Location publishedLocation = locationsRepository.find(location.getId());
        assertThat(publishedLocation.getStatus()).isEqualTo(LocationStatus.PUBLISHED);
        assertThat(publishedLocation.getLastPublishedAt()).isNotNull();
    }

    @Test
    void testLocationRejectedEvent() throws Exception {
        // given:
        final Location location = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .status(LocationStatus.AWAITING_PUBLICATION)
                        .lastPublishedAt(null)
                        .build()
        );
        final LocationMessage message = toLocationMessage(location, LocationEventType.LOCATION_REJECTED);

        // when:
        locationEventsBindings.inboundChannel().send(
                new GenericMessage<>(
                        objectMapper.writeValueAsString(message)
                )
        );

        // then:
        final Location publishedLocation = locationsRepository.find(location.getId());
        assertThat(publishedLocation.getStatus()).isEqualTo(LocationStatus.REJECTED);
        assertThat(publishedLocation.getLastPublishedAt()).isNull();
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
}