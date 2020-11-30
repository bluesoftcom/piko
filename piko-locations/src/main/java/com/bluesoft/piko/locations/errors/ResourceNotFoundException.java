package com.bluesoft.piko.locations.errors;

import java.util.Map;

public class ResourceNotFoundException extends UserErrorException {

    public ResourceNotFoundException(String resource, String id) {
        super("resource-not-found", Map.of("resource", resource, "identifier", id));
    }

}
