package com.Transporter.FreightFox.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LaneQuoteRequest(
        @NotNull(message = "laneId is required")
        Long laneId,

        @NotNull(message = "quote is required")
        @Positive(message = "quote must be positive")
        Long quote
) {
}
