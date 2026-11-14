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
import com.Transporter.FreightFox.optimizer.AssignmentOptimizer;
import com.Transporter.FreightFox.optimizer.OptimizationResult;
import com.Transporter.FreightFox.repository.LaneQuoteRepository;
import com.Transporter.FreightFox.repository.LaneRepository;
import com.Transporter.FreightFox.repository.TransporterRepository;
import com.Transporter.FreightFox.validation.InputDataValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TransporterServiceImpl implements TransporterService {

    private final LaneRepository laneRepository;
    private final TransporterRepository transporterRepository;
    private final LaneQuoteRepository laneQuoteRepository;
    private final AssignmentOptimizer assignmentOptimizer;
    private final InputDataValidator inputDataValidator;
    private final TransactionTemplate writeTransaction;
    private final TransactionTemplate readTransaction;
    private final ReadWriteLock dataLock = new ReentrantReadWriteLock(true);

    public TransporterServiceImpl(LaneRepository laneRepository,
                                  TransporterRepository transporterRepository,
                                  LaneQuoteRepository laneQuoteRepository,
                                  AssignmentOptimizer assignmentOptimizer,
                                  InputDataValidator inputDataValidator,
                                  PlatformTransactionManager transactionManager) {
        this.laneRepository = laneRepository;
        this.transporterRepository = transporterRepository;
        this.laneQuoteRepository = laneQuoteRepository;
        this.assignmentOptimizer = assignmentOptimizer;
        this.inputDataValidator = inputDataValidator;
        this.writeTransaction = new TransactionTemplate(transactionManager);
        this.readTransaction = new TransactionTemplate(transactionManager);
        this.readTransaction.setReadOnly(true);
    }

    @Override
    public ApiResponse saveInput(InputDataRequest request) {
        inputDataValidator.validate(request);

        Lock lock = dataLock.writeLock();
        lock.lock();
        try {
            writeTransaction.executeWithoutResult(status -> replaceInputData(request));
        } finally {
            lock.unlock();
        }

        return new ApiResponse("success", "Input data saved successfully.");
    }

    @Override
    public AssignmentResponse getAssignment(AssignmentRequest request) {
        Lock lock = dataLock.readLock();
        lock.lock();
        try {
            return readTransaction.execute(status -> computeAssignment(request.maxTransporters()));
        } finally {
            lock.unlock();
        }
    }

    private void replaceInputData(InputDataRequest request) {
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
    }

    private AssignmentResponse computeAssignment(int maxTransporters) {
        List<Lane> lanes = laneRepository.findAll();
        if (lanes.isEmpty()) {
            throw new DataNotFoundException("No input data found.");
        }
        List<LaneQuote> laneQuotes = laneQuoteRepository.findAllBy();
        OptimizationResult result = assignmentOptimizer.optimize(lanes, laneQuotes, maxTransporters);
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
            transporter.addLaneQuote(new LaneQuote(lanesById.get(quote.laneId()), quote.quote()));
        }
        return transporter;
    }
}
