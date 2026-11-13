package com.Transporter.FreightFox.optimizer;

import com.Transporter.FreightFox.entity.Lane;
import com.Transporter.FreightFox.entity.LaneQuote;

import java.util.List;

public interface AssignmentOptimizer {

    OptimizationResult optimize(List<Lane> lanes, List<LaneQuote> laneQuotes, int maxTransporters);
}
