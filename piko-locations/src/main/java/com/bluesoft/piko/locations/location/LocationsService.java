package com.bluesoft.piko.locations.location;

import com.bluesoft.piko.locations.auth.LoggedUser;
import com.bluesoft.piko.locations.errors.ResourceNotFoundException;
import com.bluesoft.piko.locations.errors.UserErrorException;
import com.bluesoft.piko.locations.location.json.DetailedJsonLocation;
import com.bluesoft.piko.locations.location.json.JsonLocation;
import com.bluesoft.piko.locations.location.json.JsonLocationsListing;
import com.bluesoft.piko.locations.location.messaging.LocationsGateway;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.bluesoft.piko.locations.location.LocationStatus.*;

@Service
@AllArgsConstructor
@Slf4j
public class LocationsService {

    static final int MAX_LOCATIONS_LIMIT = 5;

    private final LocationsRepository locationsRepository;
    private final LocationsGateway locationsGateway;
    private final TransactionOperations transactionOperations;

    @Transactional
    public DetailedJsonLocation createLocation(JsonLocation jsonLocation, LoggedUser user) {
        log.info("Creating location: {} by: {}", jsonLocation, user);

        if (locationsRepository.countByOwner(user.getUsername()) >= MAX_LOCATIONS_LIMIT) {
            throw new UserErrorException(
                    "user-locations-limit",
                    Map.of("limit", String.valueOf(MAX_LOCATIONS_LIMIT))
            );
        }

        final Location location = locationsRepository.save(
                Location.builder()
                        .id(UUID.randomUUID().toString())
                        .name(jsonLocation.getName())
                        .owner(user.getUsername())
                        .status(DRAFT)
                        .createdAt(Instant.now())
                        .lat(jsonLocation.getLat())
                        .lng(jsonLocation.getLng())
                        .build()
        );
        return toDetailedJsonLocation(location);
    }

    @Transactional
    public DetailedJsonLocation readLocation(String locationId, LoggedUser user) {
        log.info("Reading location: {} by user: {}", locationId, user);

        final Location location = locationsRepository.find(locationId);
        checkUserIsLocationOwner(location, user);

        return toDetailedJsonLocation(location);
    }

    @Transactional
    public JsonLocationsListing listLocations(LoggedUser user) {
        log.info("Listing locations for user: {}", user.getUsername());

        final List<Location> locations = locationsRepository.findByOwner(
                user.getUsername(),
                Sort.by(Sort.Order.asc("createdAt"))
        );

        return new JsonLocationsListing(
                locations.stream()
                        .map(LocationsService::toDetailedJsonLocation)
                        .collect(Collectors.toList())
        );
    }

    public DetailedJsonLocation updateLocation(String locationId, JsonLocation jsonLocation, LoggedUser user) {
        log.info("Updating location: {} by user: {} with data: {}", locationId, user, jsonLocation);
        final Location location = locationsRepository.find(locationId);
        checkUserIsLocationOwner(location, user);

        final LocationStatus previousStatus = location.getStatus();
        final Location updatedLocation = transactionOperations.execute(status -> {
            final Location foundLocation = locationsRepository.find(locationId);
            foundLocation.setName(jsonLocation.getName());
            foundLocation.setLat(jsonLocation.getLat());
            foundLocation.setLng(jsonLocation.getLng());
            foundLocation.setStatus(DRAFT);
            return foundLocation;
        });
        if (previousStatus == AWAITING_PUBLICATION) {
            locationsGateway.sendPublicationRequestWithdrawn(updatedLocation.getId());
        }
        return toDetailedJsonLocation(updatedLocation);
    }


    public DetailedJsonLocation updateLocationStatus(String locationId, LocationStatus requestedStatus, LoggedUser user) {
        log.info("Changing status for location: {} to: {} by: {}", locationId, requestedStatus, user);
        final Location updatedLocation = transactionOperations.execute(status -> {
            final Location location = locationsRepository.find(locationId);
            checkUserIsLocationOwner(location, user);
            checkUserRequestsLegalStatus(requestedStatus);

            location.setStatus(requestedStatus);
            return location;
        });

        if (updatedLocation.getStatus() == AWAITING_PUBLICATION) {
            locationsGateway.sendPublicationRequestIssued(updatedLocation.getId());
        }
        if (updatedLocation.getStatus() == DRAFT) {
            locationsGateway.sendPublicationRequestWithdrawn(updatedLocation.getId());
        }
        return toDetailedJsonLocation(updatedLocation);
    }


    @Transactional
    public void locationPublished(String locationId) {
        log.info("Publishing location: {}", locationId);
        final Location location = locationsRepository.find(locationId);
        location.setStatus(PUBLISHED);
        location.setLastPublishedAt(Instant.now());
    }

    @Transactional
    public void locationRejected(String locationId) {
        log.info("Rejecting location: {}", locationId);
        final Location location = locationsRepository.find(locationId);
        location.setStatus(REJECTED);
    }

    private static void checkUserRequestsLegalStatus(LocationStatus requestedStatus) {
        switch (requestedStatus) {
            case REJECTED:
            case PUBLISHED:
                throw new UserErrorException("illegal-location-state-transition");
        }
    }

    private static void checkUserIsLocationOwner(Location location, LoggedUser user) {
        if (!location.getOwner().equals(user.getUsername())) {
            throw new ResourceNotFoundException("locations", location.getId());
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
