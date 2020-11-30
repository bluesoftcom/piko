package com.bluesoft.piko.locations.location.json;

import com.bluesoft.piko.locations.location.LocationStatus;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class DetailedJsonLocation {

    String id;
    String owner;
    LocationStatus status;
    Instant lastPublishedAt;
    Instant createdAt;

    @JsonUnwrapped
    JsonLocation location;

}
