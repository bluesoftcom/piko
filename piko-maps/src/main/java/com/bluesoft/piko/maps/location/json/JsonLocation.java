package com.bluesoft.piko.maps.location.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
@Builder
public class JsonLocation {

    @NotBlank
    @Length(max = 50)
    String name;
    @NotNull
    Double lat;
    @NotNull
    Double lng;

    @JsonCreator
    public JsonLocation(@JsonProperty("name") String name,
                        @JsonProperty("lat") Double lat,
                        @JsonProperty("lng") Double lng) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

}
