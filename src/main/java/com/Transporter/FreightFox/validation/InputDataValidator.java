package com.Transporter.FreightFox.validation;

import com.Transporter.FreightFox.dto.request.InputDataRequest;
import com.Transporter.FreightFox.dto.request.LaneQuoteRequest;
import com.Transporter.FreightFox.dto.request.LaneRequest;
import com.Transporter.FreightFox.dto.request.TransporterRequest;
import com.Transporter.FreightFox.exception.InvalidInputException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class InputDataValidator {

    public void validate(InputDataRequest request) {
        List<String> errors = new ArrayList<>();

        Set<Long> laneIds = new HashSet<>();
        for (LaneRequest lane : request.lanes()) {
            if (!laneIds.add(lane.id())) {
                errors.add("duplicate lane id " + lane.id());
            }
        }

        Set<Long> transporterIds = new HashSet<>();
        for (TransporterRequest transporter : request.transporters()) {
            if (!transporterIds.add(transporter.id())) {
                errors.add("duplicate transporter id " + transporter.id());
            }

            Set<Long> quotedLaneIds = new HashSet<>();
            for (LaneQuoteRequest quote : transporter.laneQuotes()) {
                if (!laneIds.contains(quote.laneId())) {
                    errors.add("transporter " + transporter.id() + " quotes unknown lane id " + quote.laneId());
                } else if (!quotedLaneIds.add(quote.laneId())) {
                    errors.add("transporter " + transporter.id() + " has multiple quotes for lane id " + quote.laneId());
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidInputException(String.join("; ", errors));
        }
    }
}
