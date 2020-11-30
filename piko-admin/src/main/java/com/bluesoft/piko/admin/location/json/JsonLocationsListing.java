package com.bluesoft.piko.admin.location.json;

import lombok.Value;

import java.util.List;

@Value
public class JsonLocationsListing {

    List<DetailedJsonLocation> items;

}
