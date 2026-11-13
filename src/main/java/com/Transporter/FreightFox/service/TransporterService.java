package com.Transporter.FreightFox.service;

import com.Transporter.FreightFox.dto.request.AssignmentRequest;
import com.Transporter.FreightFox.dto.request.InputDataRequest;
import com.Transporter.FreightFox.dto.response.ApiResponse;
import com.Transporter.FreightFox.dto.response.AssignmentResponse;

public interface TransporterService {

    ApiResponse saveInput(InputDataRequest request);

    AssignmentResponse getAssignment(AssignmentRequest request);
}
