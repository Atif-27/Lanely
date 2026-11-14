package com.Transporter.FreightFox.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LaneRequest(
        @NotNull(message = "lane id is required")
        @Positive(message = "lane id must be positive")
        Long id,

        @NotBlank(message = "lane origin is required")
        String origin,

        @NotBlank(message = "lane destination is required")
        String destination
) {
}
