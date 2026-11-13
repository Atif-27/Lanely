package com.Transporter.FreightFox.dto.response;

import java.util.List;

public record AssignmentResponse(
        String status,
        Long totalCost,
        List<LaneAssignment> assignments,
        List<Long> selectedTransporters
) {
}
