package com.bluesoft.piko.locations.location.json;

import lombok.Value;

import java.util.List;

@Value
public class JsonLocationsListing {

    List<DetailedJsonLocation> items;

}
