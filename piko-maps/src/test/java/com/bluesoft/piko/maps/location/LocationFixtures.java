package com.bluesoft.piko.maps.location;

import com.bluesoft.piko.maps.location.json.DetailedJsonLocation;
import com.bluesoft.piko.maps.location.json.JsonLocation;

public class LocationFixtures {

    public static Location.LocationBuilder someLocation() {
        return Location.builder()
                .id("e3c2e50c-7cb3-11ea-bc55-0242ac130003")
                .lat(51)
                .lng(21)
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
                .id("e3c2e50c-7cb3-11ea-bc55-0242ac130003")
                .location(
                        someJsonLocation().build()
                )
                .owner("trevornooah");
    }

}