package com.bluesoft.piko.admin.location;

import com.bluesoft.piko.admin.auth.LoggedUser;
import com.bluesoft.piko.admin.errors.UserErrorException;
import com.bluesoft.piko.admin.location.json.DetailedJsonLocation;
import com.bluesoft.piko.admin.location.json.JsonLocation;
import com.bluesoft.piko.admin.location.json.JsonLocationsListing;
import com.bluesoft.piko.admin.location.messaging.LocationsGateway;
import com.bluesoft.piko.admin.location.notifications.AdminLocationsNotifications;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class AdminLocationsService {

    private final LocationsRepository locationsRepository;
    private final LocationsGateway locationsGateway;
    private final TransactionOperations transactionOperations;
    private final AdminLocationsNotifications adminLocationsNotifications;

    @Transactional
    public void cancelPublicationReview(String locationId) {
        log.info("Cancelling pre-publication review for location: {}", locationId);

        final Location location = locationsRepository.tryFind(locationId);

        if (location == null) {
            log.info("Cannot find location: {}. Ignoring pre-publication review cancellation", locationId);
            return;
        }
        locationsRepository.delete(location);
    }

    @Transactional
    public DetailedJsonLocation requestPublicationReview(DetailedJsonLocation location) {
        log.info("Saving location for pre-publication review: {}", location);

        final Location savedLocation = locationsRepository.save(
                Location.builder()
                        .id(location.getId())
                        .owner(location.getOwner())
                        .status(location.getStatus())
                        .createdAt(location.getCreatedAt())
                        .lat(location.getLocation().getLat())
                        .lng(location.getLocation().getLng())
                        .name(location.getLocation().getName())
                        .build()
        );

        return toDetailedJsonLocation(savedLocation);
    }

    @Transactional
    public DetailedJsonLocation adminReadLocation(String locationId, LoggedUser user) {
        log.info("Reading location: {} by user: {}", locationId, user);

        final Location location = locationsRepository.find(locationId);
        return toDetailedJsonLocation(location);
    }

    @Transactional
    public JsonLocationsListing adminListLocations(LoggedUser loggedUser) {
        log.info("Listing locations needing pre-publication review by: {}", loggedUser.getUsername());

        final List<Location> locations = locationsRepository.findAll(
                Sort.by(Sort.Order.asc("createdAt"))
        );

        return new JsonLocationsListing(
                locations.stream()
                        .map(AdminLocationsService::toDetailedJsonLocation)
                        .collect(Collectors.toList())
        );
    }

    public DetailedJsonLocation updateLocationStatus(String locationId, LocationStatus requestedStatus, LoggedUser user) {
        log.info("Changing status for location: {} to: {} by: {}", locationId, requestedStatus, user);
        checkUserRequestsLegalStatus(requestedStatus);

        final DetailedJsonLocation location = transactionOperations.execute(status -> {
            final Location foundLocation = locationsRepository.find(locationId);
            foundLocation.setStatus(requestedStatus);
            return toDetailedJsonLocation(foundLocation);
        });

        if (requestedStatus == LocationStatus.PUBLISHED) {
            locationsGateway.sendLocationPublished(location);
            adminLocationsNotifications.sendLocationPublishedEmail(location);
        }
        if (requestedStatus == LocationStatus.REJECTED) {
            locationsGateway.sendLocationRejected(location);
        }

        transactionOperations.executeWithoutResult(status ->
                locationsRepository.deleteById(locationId)
        );
        return location;
    }


    private static void checkUserRequestsLegalStatus(LocationStatus requestedStatus) {
        switch (requestedStatus) {
            case DRAFT:
            case AWAITING_PUBLICATION:
                throw new UserErrorException("illegal-location-state-transition");
            case PUBLISHED:
            case REJECTED:
        }
    }

    private static DetailedJsonLocation toDetailedJsonLocation(Location location) {
        return DetailedJsonLocation.builder()
                .id(location.getId())
                .status(location.getStatus())
                .owner(location.getOwner())
                .lastPublishedAt(location.getLastPublishedAt())
                .createdAt(location.getCreatedAt())
                .location(
                        JsonLocation.builder()
                                .name(location.getName())
                                .lat(location.getLat())
                                .lng(location.getLng())
                                .build()
                )
                .build();
    }
}
