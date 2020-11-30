package com.bluesoft.piko.admin.errors;

import com.bluesoft.piko.admin.auth.UnauthenticatedAccessAttemptException;
import com.bluesoft.piko.admin.errors.json.BadRequestResponse;
import com.bluesoft.piko.admin.errors.json.SimpleErrorResponse;
import com.bluesoft.piko.admin.errors.json.UnprocessableEntityResponse;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.MethodNotAllowedException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@RestControllerAdvice
@Component
public class GlobalErrorHandler {

    private static final EnumSet<HttpMethod> MODIFYING_METHODS = EnumSet.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SimpleErrorResponse> onException(Exception e) {
        log.error("Unexpected exception has occurred: {}. This incident should be reported", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SimpleErrorResponse("internal-server-error"));
    }

    @ExceptionHandler(UnauthenticatedAccessAttemptException.class)
    public ResponseEntity<SimpleErrorResponse> onUnauthenticatedAccessAttempt(UnauthenticatedAccessAttemptException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SimpleErrorResponse(e.getCode()));
    }

    @ExceptionHandler({HttpRequestMethodNotSupportedException.class, MethodNotAllowedException.class})
    public ResponseEntity<BadRequestResponse> onMethodNotAllowedException() {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new BadRequestResponse("method-not-allowed", List.of()));
    }

    @ExceptionHandler({JsonParseException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<BadRequestResponse> onJsonParseException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new BadRequestResponse("invalid-json-format", List.of()));
    }

    @ExceptionHandler({InvalidFormatException.class})
    public ResponseEntity<BadRequestResponse> onJsonInvalidFormatException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new BadRequestResponse("invalid-json-format", List.of()));
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<BadRequestResponse> onMissingHeader() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new BadRequestResponse("missing-http-binding", List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BadRequestResponse> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        final List<BadRequestResponse.ConstraintViolation> violations = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> {
                    final ConstraintViolation<?> violation = fieldError.unwrap(ConstraintViolation.class);
                    final String path = propertyPath(violation);
                    final String constraint = constraintName(violation);
                    return new BadRequestResponse.ConstraintViolation(path, constraint);
                })
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new BadRequestResponse("validation-error", violations));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<UnprocessableEntityResponse> onResourceNotFoundException(ResourceNotFoundException e,
                                                                                   HttpServletRequest request) {
        final HttpStatus status = MODIFYING_METHODS.stream().anyMatch(method -> method.matches(request.getMethod()))
                ? HttpStatus.UNPROCESSABLE_ENTITY
                : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status)
                .body(toUnprocessableEntityResponse(e));
    }

    @ExceptionHandler(UserErrorException.class)
    public ResponseEntity<UnprocessableEntityResponse> onUserErrorException(UserErrorException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(toUnprocessableEntityResponse(e));
    }

    private static UnprocessableEntityResponse toUnprocessableEntityResponse(UserErrorException e) {
        final List<UnprocessableEntityResponse.Detail> details = e.getDetails().entrySet().stream()
                .map(entry -> new UnprocessableEntityResponse.Detail(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return new UnprocessableEntityResponse(e.getCode(), details);
    }

    private static String propertyPath(ConstraintViolation<?> violation) {
        return StreamSupport.stream(violation.getPropertyPath().spliterator(), false)
                .map(Path.Node::getName)
                .collect(Collectors.joining("."));
    }

    private static String constraintName(ConstraintViolation<?> violation) {
        final Class<? extends Annotation> annotationClass = violation.getConstraintDescriptor().getAnnotation().annotationType();
        return toLowerHyphenCaseFormat(annotationClass.getSimpleName());
    }

    /**
     * Converts strings from upper camel case format to lower hyphen case format.
     * i.e.: "NotNull" will converted to "not-null".
     */
    private static String toLowerHyphenCaseFormat(String input) {
        if (input.isEmpty()) {
            return input;
        }
        final StringBuilder result = new StringBuilder(
                String.valueOf(Character.toLowerCase(input.charAt(0)))
        );
        for (int i = 1; i < input.length(); i++) {
            char previous = input.charAt(i - 1);
            char current = input.charAt(i);
            if (Character.isUpperCase(current) && Character.isLowerCase(previous)) {
                result.append("-");
            }
            result.append(Character.toLowerCase(current));
        }

        return result.toString();
    }

}
