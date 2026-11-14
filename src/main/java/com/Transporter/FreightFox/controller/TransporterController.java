package com.Transporter.FreightFox.controller;

import com.Transporter.FreightFox.dto.request.AssignmentRequest;
import com.Transporter.FreightFox.dto.request.InputDataRequest;
import com.Transporter.FreightFox.dto.response.AssignmentResponse;
import com.Transporter.FreightFox.dto.response.StatusResponse;
import com.Transporter.FreightFox.service.TransporterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transporters")
@RequiredArgsConstructor
@Tag(name = "Transporter Assignment", description = "Submit lane quotes and get the optimized transporter assignment")
public class TransporterController {

    private static final String INPUT_SAVED = """
            {"status": "success", "message": "Input data saved successfully."}""";
    private static final String FIELD_ERROR = """
            {"status": "error", "message": "lanes: lanes must not be empty; transporters[0].laneQuotes[0].quote: quote must be positive"}""";
    private static final String CROSS_FIELD_ERROR = """
            {"status": "error", "message": "duplicate lane id 1; transporter 1 quotes unknown lane id 9"}""";
    private static final String MALFORMED_ERROR = """
            {"status": "error", "message": "Malformed request body."}""";
    private static final String MAX_TRANSPORTERS_ERROR = """
            {"status": "error", "message": "maxTransporters: maxTransporters must be at least 1"}""";
    private static final String NO_DATA_ERROR = """
            {"status": "error", "message": "No input data found."}""";
    private static final String LIMIT_TOO_LOW_ERROR = """
            {"status": "error", "message": "Cannot cover all lanes with 2 transporter(s); at least 3 are required."}""";
    private static final String UNQUOTED_LANE_ERROR = """
            {"status": "error", "message": "No transporter has quoted for lane ids [2]."}""";
    private static final String TEST_CASE_1_RESULT = """
            {
              "status": "success",
              "totalCost": 60139,
              "assignments": [
                {"laneId": 1, "transporterId": 5},
                {"laneId": 2, "transporterId": 1},
                {"laneId": 3, "transporterId": 4},
                {"laneId": 4, "transporterId": 4},
                {"laneId": 5, "transporterId": 4}
              ],
              "selectedTransporters": [1, 4, 5]
            }""";
    private static final String TEST_CASE_2_RESULT = """
            {
              "status": "success",
              "totalCost": 7500,
              "assignments": [
                {"laneId": 1, "transporterId": 1},
                {"laneId": 2, "transporterId": 1},
                {"laneId": 3, "transporterId": 2},
                {"laneId": 4, "transporterId": 2},
                {"laneId": 5, "transporterId": 3}
              ],
              "selectedTransporters": [1, 2, 3]
            }""";

    private final TransporterService transporterService;

    @Operation(
            summary = "Submit lanes and transporter quotes",
            description = "Validates and stores the lanes and quotes. Replaces any previously submitted data.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = InputDataRequest.class),
                            examples = {
                                    @ExampleObject(name = "Test Case 1", ref = "#/components/examples/testCase1Input"),
                                    @ExampleObject(name = "Test Case 2", ref = "#/components/examples/testCase2Input")
                            }
                    )
            )
    )
    @ApiResponse(responseCode = "200", description = "Input saved",
            content = @Content(schema = @Schema(implementation = StatusResponse.class),
                    examples = @ExampleObject(value = INPUT_SAVED)))
    @ApiResponse(responseCode = "400", description = "Invalid input: field rules, duplicate ids, unknown lanes or malformed JSON",
            content = @Content(schema = @Schema(implementation = StatusResponse.class),
                    examples = {
                            @ExampleObject(name = "Field validation", value = FIELD_ERROR),
                            @ExampleObject(name = "Cross-field validation", value = CROSS_FIELD_ERROR),
                            @ExampleObject(name = "Malformed JSON", value = MALFORMED_ERROR)
                    }))
    @PostMapping("/input")
    public ResponseEntity<StatusResponse> submitInput(@Valid @RequestBody InputDataRequest request) {
        return ResponseEntity.ok(transporterService.saveInput(request));
    }

    @Operation(
            summary = "Get the optimized transporter assignment",
            description = "Returns the cheapest plan that covers every lane using at most maxTransporters transporters. "
                    + "Ties on cost prefer more transporters, then lower transporter ids.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AssignmentRequest.class),
                            examples = {
                                    @ExampleObject(name = "Test Case 1 / 2 (limit 3)", value = "{\"maxTransporters\": 3}"),
                                    @ExampleObject(name = "Test Case 2 infeasible (limit 2)", value = "{\"maxTransporters\": 2}")
                            }
                    )
            )
    )
    @ApiResponse(responseCode = "200", description = "Optimized assignment",
            content = @Content(schema = @Schema(implementation = AssignmentResponse.class),
                    examples = {
                            @ExampleObject(name = "Test Case 1 (limit 3)", value = TEST_CASE_1_RESULT),
                            @ExampleObject(name = "Test Case 2 (limit 3)", value = TEST_CASE_2_RESULT)
                    }))
    @ApiResponse(responseCode = "400", description = "maxTransporters missing or below 1",
            content = @Content(schema = @Schema(implementation = StatusResponse.class),
                    examples = @ExampleObject(value = MAX_TRANSPORTERS_ERROR)))
    @ApiResponse(responseCode = "404", description = "No input data has been submitted yet",
            content = @Content(schema = @Schema(implementation = StatusResponse.class),
                    examples = @ExampleObject(value = NO_DATA_ERROR)))
    @ApiResponse(responseCode = "422", description = "Lanes cannot all be covered within the limit",
            content = @Content(schema = @Schema(implementation = StatusResponse.class),
                    examples = {
                            @ExampleObject(name = "Limit too low", value = LIMIT_TOO_LOW_ERROR),
                            @ExampleObject(name = "Lane with no quotes", value = UNQUOTED_LANE_ERROR)
                    }))
    @PostMapping("/assignment")
    public ResponseEntity<AssignmentResponse> getAssignment(@Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(transporterService.getAssignment(request));
    }
}
