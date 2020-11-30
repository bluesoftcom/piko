package com.bluesoft.piko.admin.location.json;

import com.bluesoft.piko.admin.location.LocationStatus;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class DetailedJsonLocation {

    String id;
    LocationStatus status;
    String owner;
    Instant lastPublishedAt;
    Instant createdAt;

    @JsonUnwrapped
    JsonLocation location;

}
