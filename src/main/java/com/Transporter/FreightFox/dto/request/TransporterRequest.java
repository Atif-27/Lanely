package com.Transporter.FreightFox.dto.request;

import java.util.List;

public record TransporterRequest(
        Long id,
        String name,
        List<LaneQuoteRequest> laneQuotes
) {
}
