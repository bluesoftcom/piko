package com.bluesoft.piko.admin.location;

import com.bluesoft.piko.admin.TestTransactionOperations;
import com.bluesoft.piko.admin.auth.LoggedUser;
import com.bluesoft.piko.admin.location.json.DetailedJsonLocation;
import com.bluesoft.piko.admin.location.json.JsonLocation;
import com.bluesoft.piko.admin.location.messaging.LocationsGateway;
import com.bluesoft.piko.admin.location.notifications.AdminLocationsNotifications;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.transaction.support.TransactionOperations;

import java.time.Instant;
import java.util.UUID;

import static com.bluesoft.piko.admin.auth.LoggedUserFixtures.someLoggedUser;
import static com.bluesoft.piko.admin.location.LocationFixtures.someDetailedJsonLocation;
import static com.bluesoft.piko.admin.location.LocationFixtures.someLocation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminLocationsServiceTest {

    @InjectMocks
    private AdminLocationsService adminLocationsService;

    @Mock
    private LocationsRepository locationsRepository;
    @Mock
    private LocationsGateway locationsGateway;
    @Mock
    private AdminLocationsNotifications adminLocationsNotifications;
    @Spy
    private TransactionOperations transactionOperations = TestTransactionOperations.INSTANCE;

    @Test
    void testCreateLocation() {
        // given:
        final LoggedUser loggedUser = someLoggedUser().build();
        final DetailedJsonLocation location = someDetailedJsonLocation().build();

        when(locationsRepository.save(any())).thenAnswer(withFirstParameter());

        // when:
        final DetailedJsonLocation createdLocation = adminLocationsService.requestPublicationReview(location);

        // then:
        assertThat(createdLocation).isEqualToIgnoringGivenFields(
                someDetailedJsonLocation()
                        .owner(loggedUser.getUsername())
                        .build(),
                "id",
                "createdAt"
        );

        final Location savedLocation = captureLastSavedLocation();
        assertThat(savedLocation).isEqualToIgnoringGivenFields(
                Location.builder()
                        .id("ignored")
                        .name(location.getLocation().getName())
                        .status(location.getStatus())
                        .lat(location.getLocation().getLat())
                        .lng(location.getLocation().getLng())
                        .owner(loggedUser.getUsername())
                        .createdAt(Instant.now())
                        .build(),
                "id",
                "createdAt"
        );
        verifyNoInteractions(locationsGateway);
    }

    @Test
    void testReadLocation() {
        // given:
        final LoggedUser loggedUser = someLoggedUser().build();
        final String locationId = UUID.randomUUID().toString();

        final Location location = someLocation()
                .id(locationId)
                .owner(loggedUser.getUsername())
                .build();
        when(locationsRepository.find(any())).thenReturn(location);

        // when:
        final DetailedJsonLocation foundLocation = adminLocationsService.adminReadLocation(locationId, loggedUser);

        // then:
        assertThat(foundLocation).isEqualTo(
                DetailedJsonLocation.builder()
                        .id(locationId)
                        .status(location.getStatus())
                        .location(
                                JsonLocation.builder()
                                        .name(location.getName())
                                        .lat(location.getLat())
                                        .lng(location.getLng())
                                        .build()
                        )
                        .createdAt(location.getCreatedAt())
                        .owner(loggedUser.getUsername())
                        .build()
        );
    }

    @Test
    void testChangeLocationStatusToPublished() {
        // given:
        final LoggedUser loggedUser = someLoggedUser().build();
        final Location location = someLocation()
                .id(UUID.randomUUID().toString())
                .build();
        when(locationsRepository.find(any())).thenReturn(location);

        // when:
        final DetailedJsonLocation updatedLocation = adminLocationsService.updateLocationStatus(
                location.getId(),
                LocationStatus.PUBLISHED,
                loggedUser
        );

        // then:
        assertThat(updatedLocation.getStatus()).isEqualTo(LocationStatus.PUBLISHED);
        verify(locationsGateway).sendLocationPublished(updatedLocation);
        verify(locationsRepository).deleteById(location.getId());
        verify(adminLocationsNotifications).sendLocationPublishedEmail(updatedLocation);
    }

    @Test
    void testChangeLocationStatusToRejected() {
        // given:
        final LoggedUser loggedUser = someLoggedUser().build();
        final Location location = someLocation()
                .id(UUID.randomUUID().toString())
                .build();
        when(locationsRepository.find(any())).thenReturn(location);

        // when:
        final DetailedJsonLocation updatedLocation = adminLocationsService.updateLocationStatus(
                location.getId(),
                LocationStatus.REJECTED,
                loggedUser
        );

        // then:
        assertThat(updatedLocation.getStatus()).isEqualTo(LocationStatus.REJECTED);
        verify(locationsGateway).sendLocationRejected(updatedLocation);
        verify(locationsRepository).deleteById(location.getId());
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