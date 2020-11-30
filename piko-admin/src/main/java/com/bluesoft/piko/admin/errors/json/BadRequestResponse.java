package com.bluesoft.piko.admin.errors.json;

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
