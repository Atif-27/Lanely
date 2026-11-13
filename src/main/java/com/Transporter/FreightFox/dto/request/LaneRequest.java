package com.Transporter.FreightFox.dto.request;

public record LaneRequest(
        Long id,
        String origin,
        String destination
) {
}
