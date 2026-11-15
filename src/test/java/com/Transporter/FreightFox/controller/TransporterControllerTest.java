package com.Transporter.FreightFox.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransporterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void submitsInputAndReturnsOptimizedAssignment() throws Exception {
        postJson("/api/v1/transporters/input", testCase2Input())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Input data saved successfully."));

        postJson("/api/v1/transporters/assignment", "{\"maxTransporters\": 3}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.totalCost").value(7500))
                .andExpect(jsonPath("$.assignments.length()").value(5))
                .andExpect(jsonPath("$.selectedTransporters", contains(1, 2, 3)));
    }

    @Test
    void returnsUnprocessableWhenLimitCannotCoverAllLanes() throws Exception {
        postJson("/api/v1/transporters/input", testCase2Input()).andExpect(status().isOk());

        postJson("/api/v1/transporters/assignment", "{\"maxTransporters\": 2}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Cannot cover all lanes with 2 transporter(s); at least 3 are required."));
    }

    @Test
    void returnsBadRequestForFieldValidationErrors() throws Exception {
        postJson("/api/v1/transporters/assignment", "{\"maxTransporters\": 0}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("maxTransporters: maxTransporters must be at least 1"));
    }

    @Test
    void returnsBadRequestForCrossFieldValidationErrors() throws Exception {
        String body = """
                {
                  "lanes": [{"id": 1, "origin": "A", "destination": "B"}],
                  "transporters": [{"id": 1, "name": "T1", "laneQuotes": [{"laneId": 9, "quote": 100}]}]
                }""";

        postJson("/api/v1/transporters/input", body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("transporter 1 quotes unknown lane id 9"));
    }

    @Test
    void returnsBadRequestForMalformedJson() throws Exception {
        postJson("/api/v1/transporters/input", "{\"lanes\":")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", startsWith("Malformed")));
    }

    private ResultActions postJson(String url, String body) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private String testCase2Input() throws Exception {
        return new ClassPathResource("swagger-examples/test-case-2-input.json").getContentAsString(StandardCharsets.UTF_8);
    }
}
