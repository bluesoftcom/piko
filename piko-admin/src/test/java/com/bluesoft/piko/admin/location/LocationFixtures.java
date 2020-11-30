package com.bluesoft.piko.admin.location;

import com.bluesoft.piko.admin.location.json.DetailedJsonLocation;
import com.bluesoft.piko.admin.location.json.JsonLocation;

import java.time.Instant;

public class LocationFixtures {

    public static Location.LocationBuilder someLocation() {
        return Location.builder()
                .id("e3c2e50c-7cb3-11ea-bc55-0242ac130003")
                .lat(51)
                .lng(21)
                .status(LocationStatus.AWAITING_PUBLICATION)
                .createdAt(Instant.parse("2012-12-12T00:00:00Z"))
                .owner("trevornooah")
                .name("Corn Flower Museum");
    }

    public static JsonLocation.JsonLocationBuilder someJsonLocation() {
        return JsonLocation.builder()
                .lat(51.0)
                .lng(21.0)
                .name("Corn Flower Museum");
    }

    public static DetailedJsonLocation.DetailedJsonLocationBuilder someDetailedJsonLocation() {
        return DetailedJsonLocation.builder()
                .lastPublishedAt(null)
                .createdAt(Instant.parse("2012-12-12T00:00:00Z"))
                .id("e3c2e50c-7cb3-11ea-bc55-0242ac130003")
                .location(
                        someJsonLocation().build()
                )
                .createdAt(Instant.parse("2012-12-12T00:00:00Z"))
                .owner("trevornooah");
    }

}