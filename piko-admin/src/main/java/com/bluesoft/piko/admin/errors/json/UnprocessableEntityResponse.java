package com.bluesoft.piko.admin.errors.json;

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
