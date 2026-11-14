package com.Transporter.FreightFox.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record InputDataRequest(
        @NotEmpty(message = "lanes must not be empty")
        List<@NotNull(message = "lane must not be null") @Valid LaneRequest> lanes,

        @NotEmpty(message = "transporters must not be empty")
        List<@NotNull(message = "transporter must not be null") @Valid TransporterRequest> transporters
) {
}
