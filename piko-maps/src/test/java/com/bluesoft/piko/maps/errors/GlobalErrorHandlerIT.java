package com.bluesoft.piko.maps.errors;

import com.bluesoft.piko.maps.PikoMapsIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalErrorHandlerIT extends PikoMapsIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void testInvalidJsonFormat() throws Exception {
        // given:
        final String invalidJson = "invalid-json";

        // when & then:
        mvc.perform(post("/global-error-handler-it/test-receive-invalid-request")
                .content(invalidJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("invalid-json-format"))
                .andExpect(jsonPath("$.violations").isEmpty());
    }

    @Test
    void testInvalidDateFormat() throws Exception {
        // given:
        final String invalidJson = "{\"date\": \"2000--12-12\"}";

        // when & then:
        mvc.perform(post("/global-error-handler-it/test-receive-invalid-format")
                .content(invalidJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("invalid-json-format"))
                .andExpect(jsonPath("$.violations").isEmpty());
    }

    @Test
    void testValidationErrorsOnTheWebLayer() throws Exception {
        // given:
        final String invalidJson = "{}";

        // when & then:
        mvc.perform(post("/global-error-handler-it/test-validation-errors-on-web")
                .content(invalidJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("validation-error"))
                .andExpect(jsonPath("$.violations[0].field").value("date"))
                .andExpect(jsonPath("$.violations[0].constraint").value("not-null"));
    }

    @Test
    void testGetNonexistentResource() throws Exception {
        // when & then:
        mvc.perform(get("/global-error-handler-it/test-resource-not-found/{id}", "0123"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("resource-not-found"))
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    void testHandleUserError() throws Exception {
        // when & then:
        mvc.perform(get("/global-error-handler-it/test-user-error"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("some-user-error"))
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    void testPostNonexistentResource() throws Exception {
        // when & then:
        mvc.perform(post("/global-error-handler-it/test-resource-not-found"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("resource-not-found"))
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

}