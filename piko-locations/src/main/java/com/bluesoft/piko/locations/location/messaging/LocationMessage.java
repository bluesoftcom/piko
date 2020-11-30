package com.bluesoft.piko.locations.location.messaging;

import com.bluesoft.piko.locations.location.LocationStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class LocationMessage {

    String id;
    LocationEventType event;
    LocationStatus status;
    String owner;
    String name;
    Double lat;
    Double lng;
    Instant createdAt;

    @JsonCreator
    public LocationMessage(@JsonProperty("id") String id,
                           @JsonProperty("event") LocationEventType event,
                           @JsonProperty("status") LocationStatus status,
                           @JsonProperty("owner") String owner,
                           @JsonProperty("name") String name,
                           @JsonProperty("lat") Double lat,
                           @JsonProperty("lng") Double lng,
                           @JsonProperty("createdAt") Instant createdAt) {
        this.id = id;
        this.event = event;
        this.status = status;
        this.owner = owner;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.createdAt = createdAt;
    }
}
