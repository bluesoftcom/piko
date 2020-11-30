package com.bluesoft.piko.locations.location;

import com.bluesoft.piko.locations.auth.LoggedUser;
import com.bluesoft.piko.locations.location.json.DetailedJsonLocation;
import com.bluesoft.piko.locations.location.json.JsonLocation;
import com.bluesoft.piko.locations.location.json.JsonLocationStatusChange;
import com.bluesoft.piko.locations.location.json.JsonLocationsListing;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@Validated
public class LocationsController {

    private final LocationsService locationsService;

    @GetMapping("/locations")
    public ResponseEntity<JsonLocationsListing> listLocations(LoggedUser user) {
        final JsonLocationsListing location = locationsService.listLocations(user);
        return ResponseEntity.ok(location);
    }

    @GetMapping("/locations/{locationId}")
    public ResponseEntity<DetailedJsonLocation> readLocation(@PathVariable("locationId") String locationId,
                                                             LoggedUser user) {
        final DetailedJsonLocation location = locationsService.readLocation(locationId, user);
        return ResponseEntity.ok(location);
    }

    @PostMapping("/locations")
    public ResponseEntity<DetailedJsonLocation> createLocation(@RequestBody @Valid JsonLocation jsonLocation,
                                                               LoggedUser user) {
        final DetailedJsonLocation location = locationsService.createLocation(jsonLocation, user);
        return ResponseEntity.ok(location);
    }

    @PutMapping("/locations/{locationId}")
    public ResponseEntity<DetailedJsonLocation> updateLocation(@PathVariable("locationId") String locationId,
                                                               @RequestBody @Valid JsonLocation jsonLocation,
                                                               LoggedUser user) {
        final DetailedJsonLocation location = locationsService.updateLocation(locationId, jsonLocation, user);
        return ResponseEntity.ok(location);
    }

    @PutMapping("/locations/{locationId}/status")
    public ResponseEntity<DetailedJsonLocation> updateLocationStatus(@PathVariable("locationId") String locationId,
                                                                     @RequestBody @Valid JsonLocationStatusChange statusChange,
                                                                     LoggedUser user) {
        final DetailedJsonLocation location = locationsService.updateLocationStatus(locationId, statusChange.getStatus(), user);
        return ResponseEntity.ok(location);
    }

}
