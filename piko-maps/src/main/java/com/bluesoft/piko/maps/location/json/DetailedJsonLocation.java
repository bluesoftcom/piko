package com.bluesoft.piko.maps.location.json;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DetailedJsonLocation {

    String id;
    String owner;

    @JsonUnwrapped
    JsonLocation location;

}
