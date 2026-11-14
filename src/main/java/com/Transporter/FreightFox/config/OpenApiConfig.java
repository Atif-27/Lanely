package com.Transporter.FreightFox.config;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

@Configuration
public class OpenApiConfig {

    private static final String DESCRIPTION = """
            Assigns transporters to lanes so that every lane is covered at the lowest total cost, \
            using at most `maxTransporters` transporters.

            **Flow:** submit lanes and quotes with `POST /input`, then request a plan with `POST /assignment`.

            **Assumptions**
            - Each lane is assigned to exactly one transporter: the cheapest quote among the selected transporters.
            - Total cost is the sum of the assigned quote for every lane.
            - Plans are ranked by lowest total cost, then by more transporters used, then by the lowest transporter ids. \
            More transporters are used only when it does not increase cost.
            - A transporter counts as used only if it wins at least one lane, so fewer than `maxTransporters` may be selected.
            - If two selected transporters quote the same price for a lane, the lower transporter id wins it.
            - Each new input submission replaces the previous data. Data is held in an in-memory H2 database and is lost on restart.
            - If a lane has no quotes, or the limit is too low to cover every lane, the API returns 422 \
            with the minimum number of transporters required.
            - The optimizer is exact and checks every combination within the limit, which suits small to medium inputs.
            - The sample responses in the assignment brief are illustrative. For Test Case 1 with `maxTransporters = 3` \
            the true optimum is 60139 using transporters [1, 4, 5].
            """;

    @Bean
    public OpenAPI transporterAssignmentOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Transporter Assignment on Lanes API")
                        .version("v1")
                        .description(DESCRIPTION))
                .components(new Components()
                        .addExamples("testCase1Input", loadExample("Test Case 1", "swagger-examples/test-case-1-input.json"))
                        .addExamples("testCase2Input", loadExample("Test Case 2", "swagger-examples/test-case-2-input.json")));
    }

    private Example loadExample(String summary, String path) {
        try (InputStream input = new ClassPathResource(path).getInputStream()) {
            return new Example().summary(summary).value(Json.mapper().readTree(input));
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not load Swagger example " + path, ex);
        }
    }
}
