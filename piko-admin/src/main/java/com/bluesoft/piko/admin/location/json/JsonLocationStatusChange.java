package com.bluesoft.piko.admin.location.json;

import com.bluesoft.piko.admin.location.LocationStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class JsonLocationStatusChange {

    @NotNull
    LocationStatus status;

    @JsonCreator
    public JsonLocationStatusChange(@JsonProperty("status") LocationStatus status) {
        this.status = status;
    }
}
