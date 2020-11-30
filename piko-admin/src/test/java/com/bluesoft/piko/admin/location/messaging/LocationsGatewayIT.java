package com.bluesoft.piko.admin.location.messaging;

import com.bluesoft.piko.admin.PikoAdminIT;
import com.bluesoft.piko.admin.location.LocationsRepository;
import com.bluesoft.piko.admin.location.json.DetailedJsonLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.bluesoft.piko.admin.location.LocationFixtures.someDetailedJsonLocation;
import static org.assertj.core.api.Assertions.assertThat;

class LocationsGatewayIT extends PikoAdminIT {

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
    void testSendLocationPublished() {
        // given:
        final DetailedJsonLocation location = someDetailedJsonLocation().build();

        // when:
        locationsGateway.sendLocationPublished(location);

        // then:
        final LocationMessage message = locationEventsMessageCollector.captureLastLocationMessage();
        assertThat(message).isEqualTo(
                LocationMessage.builder()
                        .id(location.getId())
                        .event(LocationEventType.LOCATION_PUBLISHED)
                        .lat(location.getLocation().getLat())
                        .lng(location.getLocation().getLng())
                        .createdAt(location.getCreatedAt())
                        .owner(location.getOwner())
                        .name(location.getLocation().getName())
                        .build()
        );
    }

    @Test
    void testSendLocationRejected() {
        // given:
        final DetailedJsonLocation location = someDetailedJsonLocation().build();

        // when:
        locationsGateway.sendLocationRejected(location);

        // then:
        final LocationMessage message = locationEventsMessageCollector.captureLastLocationMessage();
        assertThat(message).isEqualTo(
                LocationMessage.builder()
                        .id(location.getId())
                        .event(LocationEventType.LOCATION_REJECTED)
                        .lat(location.getLocation().getLat())
                        .lng(location.getLocation().getLng())
                        .createdAt(location.getCreatedAt())
                        .owner(location.getOwner())
                        .name(location.getLocation().getName())
                        .build()
        );
    }

}