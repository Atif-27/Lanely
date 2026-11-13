package com.Transporter.FreightFox.optimizer;

import com.Transporter.FreightFox.dto.response.LaneAssignment;

import java.util.List;

public record OptimizationResult(
        long totalCost,
        List<LaneAssignment> assignments,
        List<Long> selectedTransporters
) {
}
