package com.bluesoft.piko.maps.location;

import com.bluesoft.piko.maps.location.json.DetailedJsonLocation;
import com.bluesoft.piko.maps.location.json.JsonLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static com.bluesoft.piko.maps.location.LocationFixtures.someDetailedJsonLocation;
import static com.bluesoft.piko.maps.location.LocationFixtures.someLocation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationsServiceTest {

    @InjectMocks
    private LocationsService locationsService;

    @Mock
    private LocationsRepository locationsRepository;

    @Test
    void testLocationPublished() {
        // given:
        final DetailedJsonLocation location = someDetailedJsonLocation().build();
        when(locationsRepository.save(any())).thenAnswer(withFirstParameter());

        // when:
        final DetailedJsonLocation createdLocation = locationsService.locationPublished(location);

        // then:
        assertThat(createdLocation).isEqualTo(location);

        final Location savedLocation = captureLastSavedLocation();
        assertThat(savedLocation).isEqualToIgnoringGivenFields(
                Location.builder()
                        .id(location.getId())
                        .name(location.getLocation().getName())
                        .lat(location.getLocation().getLat())
                        .lng(location.getLocation().getLng())
                        .owner(location.getOwner())
                        .build()
        );
    }

    @Test
    void testReadLocation() {
        // given:
        final Location location = someLocation().build();
        when(locationsRepository.find(any())).thenReturn(location);

        // when:
        final DetailedJsonLocation foundLocation = locationsService.readLocation(location.getId());

        // then:
        assertThat(foundLocation).isEqualTo(
                DetailedJsonLocation.builder()
                        .id(location.getId())
                        .location(
                                JsonLocation.builder()
                                        .name(location.getName())
                                        .lat(location.getLat())
                                        .lng(location.getLng())
                                        .build()
                        )
                        .owner(location.getOwner())
                        .build()
        );
    }

    private Location captureLastSavedLocation() {
        final ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
        verify(locationsRepository).save(captor.capture());
        return captor.getValue();
    }

    private static <T> Answer<T> withFirstParameter() {
        return answer -> answer.getArgument(0);
    }
}