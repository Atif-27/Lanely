package com.Transporter.FreightFox.dto.request;

import java.util.List;

public record InputDataRequest(
        List<LaneRequest> lanes,
        List<TransporterRequest> transporters
) {
}
