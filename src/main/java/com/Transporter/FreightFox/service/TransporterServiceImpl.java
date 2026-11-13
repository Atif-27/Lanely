package com.Transporter.FreightFox.service;

import com.Transporter.FreightFox.dto.request.AssignmentRequest;
import com.Transporter.FreightFox.dto.request.InputDataRequest;
import com.Transporter.FreightFox.dto.request.LaneQuoteRequest;
import com.Transporter.FreightFox.dto.request.TransporterRequest;
import com.Transporter.FreightFox.dto.response.ApiResponse;
import com.Transporter.FreightFox.dto.response.AssignmentResponse;
import com.Transporter.FreightFox.entity.Lane;
import com.Transporter.FreightFox.entity.LaneQuote;
import com.Transporter.FreightFox.entity.Transporter;
import com.Transporter.FreightFox.exception.DataNotFoundException;
import com.Transporter.FreightFox.exception.InvalidInputException;
import com.Transporter.FreightFox.optimizer.AssignmentOptimizer;
import com.Transporter.FreightFox.optimizer.OptimizationResult;
import com.Transporter.FreightFox.repository.LaneQuoteRepository;
import com.Transporter.FreightFox.repository.LaneRepository;
import com.Transporter.FreightFox.repository.TransporterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransporterServiceImpl implements TransporterService {

    private final LaneRepository laneRepository;
    private final TransporterRepository transporterRepository;
    private final LaneQuoteRepository laneQuoteRepository;
    private final AssignmentOptimizer assignmentOptimizer;

    @Override
    @Transactional
    public ApiResponse saveInput(InputDataRequest request) {
        laneQuoteRepository.deleteAllInBatch();
        transporterRepository.deleteAllInBatch();
        laneRepository.deleteAllInBatch();

        List<Lane> lanes = request.lanes().stream()
                .map(lane -> new Lane(lane.id(), lane.origin(), lane.destination()))
                .toList();
        Map<Long, Lane> lanesById = laneRepository.saveAll(lanes).stream()
                .collect(Collectors.toMap(Lane::getId, Function.identity()));

        List<Transporter> transporters = request.transporters().stream()
                .map(transporter -> toTransporter(transporter, lanesById))
                .toList();
        transporterRepository.saveAll(transporters);

        return new ApiResponse("success", "Input data saved successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignment(AssignmentRequest request) {
        List<Lane> lanes = laneRepository.findAll();
        if (lanes.isEmpty()) {
            throw new DataNotFoundException("No input data found.");
        }
        List<LaneQuote> laneQuotes = laneQuoteRepository.findAllBy();
        OptimizationResult result = assignmentOptimizer.optimize(lanes, laneQuotes, request.maxTransporters());
        return new AssignmentResponse(
                "success",
                result.totalCost(),
                result.assignments(),
                result.selectedTransporters()
        );
    }

    private Transporter toTransporter(TransporterRequest request, Map<Long, Lane> lanesById) {
        Transporter transporter = new Transporter(request.id(), request.name());
        for (LaneQuoteRequest quote : request.laneQuotes()) {
            Lane lane = lanesById.get(quote.laneId());
            if (lane == null) {
                throw new InvalidInputException("Unknown lane id " + quote.laneId() + " for transporter " + request.id());
            }
            transporter.addLaneQuote(new LaneQuote(lane, quote.quote()));
        }
        return transporter;
    }
}
