package com.bluesoft.piko.locations.errors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@RestController
@AllArgsConstructor
class TestController {

    @PostMapping("/global-error-handler-it/test-receive-invalid-request")
    public ResponseEntity<TestRequest> testReceiveInvalidRequest(@RequestBody TestRequest request) {
        return ResponseEntity.ok(request);
    }

    @PostMapping("/global-error-handler-it/test-receive-invalid-format")
    public ResponseEntity<TestRequest> testReceiveInvalidFormat(@RequestBody TestRequest request) {
        return ResponseEntity.ok(request);
    }

    @PostMapping("/global-error-handler-it/test-validation-errors-on-web")
    public ResponseEntity<TestRequest> testValidationErrorsOnWeb(@RequestBody @Valid TestRequest request) {
        return ResponseEntity.ok(request);
    }

    @GetMapping("/global-error-handler-it/test-resource-not-found/{id}")
    public ResponseEntity<TestRequest> testResourceDoesNotExist(@PathVariable("id") String id) {
        throw new ResourceNotFoundException("projects", id);
    }

    @GetMapping("/global-error-handler-it/test-user-error")
    public ResponseEntity<TestRequest> testUserError() {
        throw new UserErrorException("some-user-error");
    }

    @PostMapping("/global-error-handler-it/test-resource-not-found")
    public ResponseEntity<TestRequest> testCreateResourceDoesNotExist() {
        throw new ResourceNotFoundException("projects", "0123");
    }

    @Value
    public static class TestRequest {

        @Nullable
        String message;
        @NotNull
        LocalDate date;

        @JsonCreator
        public TestRequest(@Nullable @JsonProperty("message") String message,
                           @JsonProperty("date") LocalDate date) {
            this.message = message;
            this.date = date;
        }
    }
}
