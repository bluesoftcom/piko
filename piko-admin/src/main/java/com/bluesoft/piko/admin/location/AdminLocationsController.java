package com.bluesoft.piko.admin.location;

import com.bluesoft.piko.admin.auth.LoggedUser;
import com.bluesoft.piko.admin.location.json.DetailedJsonLocation;
import com.bluesoft.piko.admin.location.json.JsonLocationStatusChange;
import com.bluesoft.piko.admin.location.json.JsonLocationsListing;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@Validated
public class AdminLocationsController {

    private final AdminLocationsService adminLocationsService;

    @GetMapping("/locations")
    public ResponseEntity<JsonLocationsListing> listLocations(LoggedUser user) {
        final JsonLocationsListing location = adminLocationsService.adminListLocations(user);
        return ResponseEntity.ok(location);
    }

    @GetMapping("/locations/{locationId}")
    public ResponseEntity<DetailedJsonLocation> readLocation(@PathVariable("locationId") String locationId,
                                                             LoggedUser user) {
        final DetailedJsonLocation location = adminLocationsService.adminReadLocation(locationId, user);
        return ResponseEntity.ok(location);
    }

    @PutMapping("/locations/{locationId}/status")
    public ResponseEntity<DetailedJsonLocation> updateLocationStatus(@PathVariable("locationId") String locationId,
                                                                     @RequestBody @Valid JsonLocationStatusChange statusChange,
                                                                     LoggedUser user) {
        final DetailedJsonLocation location = adminLocationsService.updateLocationStatus(locationId, statusChange.getStatus(), user);
        return ResponseEntity.ok(location);
    }
}
