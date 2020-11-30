package com.bluesoft.piko.maps.location.messaging;

import com.bluesoft.piko.maps.PikoMapsIT;
import com.bluesoft.piko.maps.location.Location;
import com.bluesoft.piko.maps.location.LocationStatus;
import com.bluesoft.piko.maps.location.LocationsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.support.GenericMessage;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LocationEventsListenerIT extends PikoMapsIT {

    @Autowired
    private LocationEventsBindings locationEventsBindings;
    @Autowired
    private MessageCollector messageCollector;
    @Autowired
    private LocationsRepository locationsRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLocationPublished() throws Exception {
        // given:
        final LocationMessage message = someLocationMessage()
                .id(UUID.randomUUID().toString())
                .event(LocationEventType.LOCATION_PUBLISHED)
                .build();

        // when:
        locationEventsBindings.inboundChannel().send(
                new GenericMessage<>(
                        objectMapper.writeValueAsString(message)
                )
        );

        // then:
        final Location location = locationsRepository.find(message.getId());
        assertThat(location).isEqualTo(
                Location.builder()
                        .id(message.getId())
                        .name(message.getName())
                        .lat(message.getLat())
                        .lng(message.getLng())
                        .owner(message.getOwner())
                        .build()
        );
    }


    private static LocationMessage.LocationMessageBuilder someLocationMessage() {
        return LocationMessage.builder()
                .id("e3c2e50c-7cb3-11ea-bc55-0242ac130003")
                .lat(51.1)
                .lng(21.23)
                .status(LocationStatus.AWAITING_PUBLICATION)
                .createdAt(Instant.parse("2012-12-12T00:00:00Z"))
                .owner("trevornooah")
                .name("Corn Flower Museum")
                .event(LocationEventType.LOCATION_PUBLISHED);
    }
}