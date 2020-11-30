package com.bluesoft.piko.locations.location;

import com.bluesoft.piko.locations.TestTransactionOperations;
import com.bluesoft.piko.locations.auth.LoggedUser;
import com.bluesoft.piko.locations.errors.ResourceNotFoundException;
import com.bluesoft.piko.locations.errors.UserErrorException;
import com.bluesoft.piko.locations.location.json.DetailedJsonLocation;
import com.bluesoft.piko.locations.location.json.JsonLocation;
import com.bluesoft.piko.locations.location.messaging.LocationsGateway;
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

import static com.bluesoft.piko.locations.auth.LoggedUserFixtures.someLoggedUser;
import static com.bluesoft.piko.locations.location.LocationFixtures.*;
import static com.bluesoft.piko.locations.location.LocationsService.MAX_LOCATIONS_LIMIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationsServiceTest {

    @InjectMocks
    private LocationsService locationsService;

    @Mock
    private LocationsRepository locationsRepository;
    @Mock
    private LocationsGateway locationsGateway;
    @Spy
    private TransactionOperations transactionOperations = TestTransactionOperations.INSTANCE;

    @Test
    void testCreateLocation() {
        // given:
        final LoggedUser loggedUser = someLoggedUser().build();
        final JsonLocation location = someJsonLocation().build();

        when(locationsRepository.countByOwner(any())).thenReturn(0);
        when(locationsRepository.save(any())).thenAnswer(withFirstParameter());

        // when:
        final DetailedJsonLocation createdLocation = locationsService.createLocation(location, loggedUser);

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
                        .status(LocationStatus.DRAFT)
                        .name(location.getName())
                        .lat(location.getLat())
                        .lng(location.getLng())
                        .owner(loggedUser.getUsername())
                        .createdAt(Instant.now())
                        .build(),
                "id",
                "createdAt"
        );
        verifyNoInteractions(locationsGateway);
    }

    @Test
    void testCreateLocationOverUserLimit() {
        // given:
        final LoggedUser loggedUser = someLoggedUser().build();
        final JsonLocation location = someJsonLocation().build();

        when(locationsRepository.countByOwner(any())).thenReturn(MAX_LOCATIONS_LIMIT);

        // when:
        final UserErrorException exception = catchThrowableOfType(
                () -> locationsService.createLocation(location, loggedUser),
                UserErrorException.class
        );

        // then:
        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo("user-locations-limit");
    }

    @Test
    void testReadLocation() {
        // given:
        final LoggedUser loggedUser = someLoggedUser().build();

        final Location location = someLocation()
                .owner(loggedUser.getUsername())
                .build();
        when(locationsRepository.find(any())).thenReturn(location);

        // when:
        final DetailedJsonLocation foundLocation = locationsService.readLocation(location.getId(), loggedUser);

        // then:
        assertThat(foundLocation).isEqualTo(
                DetailedJsonLocation.builder()
                        .id(location.getId())
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
    void testReadLocationOwnedByOtherUser() {
        // given:
        final LoggedUser loggedUser1 = someLoggedUser()
                .username("user1")
                .build();
        final LoggedUser loggedUser2 = someLoggedUser()
                .username("user2")
                .build();

        final Location location = someLocation()
                .owner(loggedUser1.getUsername())
                .build();
        when(locationsRepository.find(any())).thenReturn(location);

        // when:
        final ResourceNotFoundException exception = catchThrowableOfType(
                () -> locationsService.readLocation(location.getId(), loggedUser2),
                ResourceNotFoundException.class
        );

        // then:
        assertThat(exception).isNotNull();
    }

    @Test
    void testUpdateLocationOwnerByOtherUser() {
        // given:
        final LoggedUser loggedUser1 = someLoggedUser()
                .username("user1")
                .build();
        final LoggedUser loggedUser2 = someLoggedUser()
                .username("user2")
                .build();

        final Location location = someLocation()
                .owner(loggedUser1.getUsername())
                .build();
        when(locationsRepository.find(any())).thenReturn(location);

        final JsonLocation updatedLocation = someJsonLocation()
                .name("Guadalajara Mansion")
                .build();

        // when:
        final ResourceNotFoundException caughtException = catchThrowableOfType(
                () -> locationsService.updateLocation(location.getId(), updatedLocation, loggedUser2),
                ResourceNotFoundException.class
        );

        // then:
        assertThat(caughtException).isNotNull();
    }

    @Test
    void testUpdateLocation() {
        // given:
        final LoggedUser loggedUser = someLoggedUser().build();

        final Location location = someLocation()
                .status(LocationStatus.DRAFT)
                .owner(loggedUser.getUsername())
                .build();
        when(locationsRepository.find(any())).thenReturn(location);

        final JsonLocation locationUpdate = someJsonLocation()
                .name("Guadalajara Mansion")
                .lat(21.2)
                .lng(-23.3)
                .build();

        // when:
        final DetailedJsonLocation updatedLocation = locationsService.updateLocation(location.getId(), locationUpdate, loggedUser);

        // then:
        assertThat(updatedLocation).isEqualTo(
                DetailedJsonLocation.builder()
                        .id(location.getId())
                        .status(LocationStatus.DRAFT)
                        .location(locationUpdate)
                        .createdAt(location.getCreatedAt())
                        .owner(loggedUser.getUsername())
                        .build()
        );
    }

    @Test
    void testUpdateDraftLocation() {
        // given:
        final LoggedUser loggedUser = someLoggedUser().build();
        final String locationId = UUID.randomUUID().toString();

        final Location location = someLocation()
                .id(locationId)
                .status(LocationStatus.DRAFT)
                .owner(loggedUser.getUsername())
                .build();
        when(locationsRepository.find(any())).thenReturn(location);

        final JsonLocation locationUpdate = someJsonLocation()
                .name("Guadalajara Mansion")
                .lat(21.2)
                .lng(-23.3)
                .build();

        // when:
        locationsService.updateLocation(locationId, locationUpdate, loggedUser);

        // then:
        verifyNoInteractions(locationsGateway);
    }

    @Test
    void testUpdateLocationAwaitingForPublication() {
        // given:
        final LoggedUser loggedUser = someLoggedUser().build();
        final String locationId = UUID.randomUUID().toString();

        final Location location = someLocation()
                .id(locationId)
                .status(LocationStatus.AWAITING_PUBLICATION)
                .owner(loggedUser.getUsername())
                .build();
        when(locationsRepository.find(any())).thenReturn(location);

        final JsonLocation locationUpdate = someJsonLocation()
                .name("Guadalajara Mansion")
                .lat(21.2)
                .lng(-23.3)
                .build();

        // when:
        locationsService.updateLocation(locationId, locationUpdate, loggedUser);

        // then:
        verify(locationsGateway).sendPublicationRequestWithdrawn(locationId);
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
        final UserErrorException exception = catchThrowableOfType(
                () -> locationsService.updateLocationStatus(
                        location.getId(),
                        LocationStatus.PUBLISHED,
                        loggedUser
                ),
                UserErrorException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo("illegal-location-state-transition");
    }

    @Test
    void testChangeStatusOfOtherUsersLocation() {
        // given:
        final LoggedUser loggedUser1 = someLoggedUser()
                .username("user1")
                .build();
        final LoggedUser loggedUser2 = someLoggedUser()
                .username("user2")
                .build();
        final Location location = someLocation()
                .id(UUID.randomUUID().toString())
                .owner(loggedUser1.getUsername())
                .build();
        when(locationsRepository.find(any())).thenReturn(location);

        // when:
        final UserErrorException exception = catchThrowableOfType(
                () -> locationsService.updateLocationStatus(
                        location.getId(),
                        LocationStatus.AWAITING_PUBLICATION,
                        loggedUser2
                ),
                UserErrorException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo("resource-not-found");
    }

    @Test
    void testChangeLocationStatusToAwaitingForPublication() {
        // given:
        final LoggedUser loggedUser = someLoggedUser().build();
        final Location location = someLocation().build();
        when(locationsRepository.find(any())).thenReturn(location);

        // when:
        final DetailedJsonLocation updatedLocation = locationsService.updateLocationStatus(
                location.getId(),
                LocationStatus.AWAITING_PUBLICATION,
                loggedUser
        );

        // then:
        assertThat(updatedLocation.getStatus()).isEqualTo(LocationStatus.AWAITING_PUBLICATION);
        verify(locationsGateway).sendPublicationRequestIssued(location.getId());
    }

    @Test
    void testChangeLocationStatusToDraft() {
        // given:
        final LoggedUser loggedUser = someLoggedUser().build();
        final Location location = someLocation()
                .id(UUID.randomUUID().toString())
                .build();
        when(locationsRepository.find(any())).thenReturn(location);

        // when:
        final DetailedJsonLocation updatedLocation = locationsService.updateLocationStatus(
                location.getId(),
                LocationStatus.DRAFT,
                loggedUser
        );

        // then:
        assertThat(updatedLocation.getStatus()).isEqualTo(LocationStatus.DRAFT);
        verify(locationsGateway).sendPublicationRequestWithdrawn(location.getId());
    }

    @Test
    void testLocationPublished() {
        // given:
        final Location location = someLocation()
                .status(LocationStatus.AWAITING_PUBLICATION)
                .lastPublishedAt(null)
                .build();
        when(locationsRepository.find(any())).thenReturn(location);

        // when:
        locationsService.locationPublished(location.getId());

        // then:
        assertThat(location.getStatus()).isEqualTo(LocationStatus.PUBLISHED);
        assertThat(location.getLastPublishedAt()).isNotNull();
    }

    @Test
    void testLocationRejected() {
        // given:
        final Location location = someLocation()
                .status(LocationStatus.AWAITING_PUBLICATION)
                .lastPublishedAt(null)
                .build();
        when(locationsRepository.find(any())).thenReturn(location);

        // when:
        locationsService.locationRejected(location.getId());

        // then:
        assertThat(location.getStatus()).isEqualTo(LocationStatus.REJECTED);
        assertThat(location.getLastPublishedAt()).isNull();
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