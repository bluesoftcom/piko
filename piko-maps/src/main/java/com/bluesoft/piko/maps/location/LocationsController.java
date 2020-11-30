package com.bluesoft.piko.maps.location;

import com.bluesoft.piko.maps.location.json.DetailedJsonLocation;
import com.bluesoft.piko.maps.location.json.JsonLocationsListing;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Validated
public class LocationsController {

    private final LocationsService locationsService;

    @GetMapping("/locations")
    public ResponseEntity<JsonLocationsListing> listLocations() {
        final JsonLocationsListing location = locationsService.listLocations();
        return ResponseEntity.ok(location);
    }

    @GetMapping("/locations/{locationId}")
    public ResponseEntity<DetailedJsonLocation> readLocation(@PathVariable("locationId") String locationId) {
        final DetailedJsonLocation location = locationsService.readLocation(locationId);
        return ResponseEntity.ok(location);
    }

}
