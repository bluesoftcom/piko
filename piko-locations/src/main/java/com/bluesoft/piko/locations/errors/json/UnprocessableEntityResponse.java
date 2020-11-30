package com.bluesoft.piko.locations.errors.json;

import lombok.Value;

import java.util.List;

@Value
public class UnprocessableEntityResponse {

    String code;
    List<Detail> details;

    @Value
    public static class Detail {
        String name;
        String value;
    }

}
