package com.Transporter.FreightFox.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record TransporterRequest(
        @NotNull(message = "transporter id is required")
        @Positive(message = "transporter id must be positive")
        Long id,

        @NotBlank(message = "transporter name is required")
        String name,

        @NotEmpty(message = "laneQuotes must not be empty")
        List<@NotNull(message = "laneQuote must not be null") @Valid LaneQuoteRequest> laneQuotes
) {
}
