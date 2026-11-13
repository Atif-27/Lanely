package com.Transporter.FreightFox.optimizer;

import com.Transporter.FreightFox.entity.Lane;
import com.Transporter.FreightFox.entity.LaneQuote;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransporterAssignmentOptimizer implements AssignmentOptimizer {

    @Override
    public OptimizationResult optimize(List<Lane> lanes, List<LaneQuote> laneQuotes, int maxTransporters) {
        return new OptimizationResult(0L, List.of(), List.of());
    }
}
