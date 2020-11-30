package com.bluesoft.piko.maps.location;

import com.bluesoft.piko.maps.location.json.DetailedJsonLocation;
import com.bluesoft.piko.maps.location.json.JsonLocation;
import com.bluesoft.piko.maps.location.json.JsonLocationsListing;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class LocationsService {

    private final LocationsRepository locationsRepository;

    public DetailedJsonLocation readLocation(String locationId) {
        log.info("Reading location: {}", locationId);

        final Location location = locationsRepository.find(locationId);
        return toDetailedJsonLocation(location);
    }

    public JsonLocationsListing listLocations() {
        log.info("Listing locations");
        final List<Location> locations = locationsRepository.findAll(
                Sort.by(Sort.Order.asc("name"))
        );

        return new JsonLocationsListing(
                locations.stream()
                        .map(LocationsService::toDetailedJsonLocation)
                        .collect(Collectors.toList())
        );
    }

    public DetailedJsonLocation locationPublished(DetailedJsonLocation location) {
        log.info("Publishing location: {}", location);
        final Location createdLocation = locationsRepository.save(
                Location.builder()
                        .id(location.getId())
                        .name(location.getLocation().getName())
                        .owner(location.getOwner())
                        .lat(location.getLocation().getLat())
                        .lng(location.getLocation().getLng())
                        .build()

        );

        return toDetailedJsonLocation(createdLocation);
    }

    private static DetailedJsonLocation toDetailedJsonLocation(Location location) {
        return DetailedJsonLocation.builder()
                .id(location.getId())
                .owner(location.getOwner())
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
