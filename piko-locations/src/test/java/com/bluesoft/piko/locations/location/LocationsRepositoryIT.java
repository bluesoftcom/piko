package com.bluesoft.piko.locations.location;

import com.bluesoft.piko.locations.PikoLocationsIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.bluesoft.piko.locations.location.LocationFixtures.someLocation;
import static org.assertj.core.api.Assertions.assertThat;

class LocationsRepositoryIT extends PikoLocationsIT {

    @Autowired
    private LocationsRepository locationsRepository;

    @Test
    void testFindByOwner() {
        // given:
        final String owner = "owner:" + UUID.randomUUID().toString();
        final Location location1 = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .createdAt(Instant.parse("2020-03-03T12:00:00Z"))
                        .owner(owner)
                        .build()
        );
        final Location location2 = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .createdAt(Instant.parse("2020-03-03T13:00:00Z"))
                        .owner(owner)
                        .build()
        );
        final Location location3 = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .owner("other-user")
                        .build()
        );

        // when:
        final List<Location> locations = locationsRepository.findByOwner(owner, Sort.by("createdAt"));

        // then:
        assertThat(locations).containsExactly(location1, location2);
    }
}