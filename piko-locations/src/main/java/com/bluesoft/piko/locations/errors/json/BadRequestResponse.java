package com.bluesoft.piko.locations.errors.json;

import lombok.Value;

import java.util.List;

@Value
public class BadRequestResponse {

    String code;
    List<ConstraintViolation> violations;

    @Value
    public static class ConstraintViolation {
        String field;
        String constraint;
    }
}
