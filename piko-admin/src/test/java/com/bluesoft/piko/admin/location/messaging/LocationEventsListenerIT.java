package com.bluesoft.piko.admin.location.messaging;

import com.bluesoft.piko.admin.PikoAdminIT;
import com.bluesoft.piko.admin.location.Location;
import com.bluesoft.piko.admin.location.LocationStatus;
import com.bluesoft.piko.admin.location.LocationsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.support.GenericMessage;

import java.time.Instant;
import java.util.UUID;

import static com.bluesoft.piko.admin.location.LocationFixtures.someLocation;
import static org.assertj.core.api.Assertions.assertThat;

class LocationEventsListenerIT extends PikoAdminIT {

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
    void testLocationPublicationRequestIssued() throws Exception {
        // given:
        final LocationMessage message = someLocationMessage()
                .id(UUID.randomUUID().toString())
                .event(LocationEventType.PUBLICATION_REQUEST_ISSUED)
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
                        .status(message.getStatus())
                        .createdAt(message.getCreatedAt())
                        .owner(message.getOwner())
                        .lastPublishedAt(null)
                        .build()
        );
    }

    @Test
    void testLocationPublicationRequestWithdrawn() throws Exception {
        // given:
        final Location location = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .status(LocationStatus.AWAITING_PUBLICATION)
                        .lastPublishedAt(null)
                        .build()
        );
        final LocationMessage message = toLocationMessage(location, LocationEventType.PUBLICATION_REQUEST_WITHDRAWN);

        // when:
        locationEventsBindings.inboundChannel().send(
                new GenericMessage<>(
                        objectMapper.writeValueAsString(message)
                )
        );

        // then:
        final Location withdrawnLocation = locationsRepository.tryFind(location.getId());
        assertThat(withdrawnLocation).isNull();
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


    private static LocationMessage.LocationMessageBuilder someLocationMessage() {
        return LocationMessage.builder()
                .id("e3c2e50c-7cb3-11ea-bc55-0242ac130003")
                .lat(51.1)
                .lng(21.23)
                .status(LocationStatus.AWAITING_PUBLICATION)
                .createdAt(Instant.parse("2012-12-12T00:00:00Z"))
                .owner("trevornooah")
                .name("Corn Flower Museum")
                .event(LocationEventType.PUBLICATION_REQUEST_ISSUED);
    }
}