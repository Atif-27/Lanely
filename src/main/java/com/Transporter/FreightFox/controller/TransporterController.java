package com.Transporter.FreightFox.controller;

import com.Transporter.FreightFox.dto.request.AssignmentRequest;
import com.Transporter.FreightFox.dto.request.InputDataRequest;
import com.Transporter.FreightFox.dto.response.ApiResponse;
import com.Transporter.FreightFox.dto.response.AssignmentResponse;
import com.Transporter.FreightFox.service.TransporterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transporters")
@RequiredArgsConstructor
public class TransporterController {

    private final TransporterService transporterService;

    @PostMapping("/input")
    public ResponseEntity<ApiResponse> submitInput(@Valid @RequestBody InputDataRequest request) {
        return ResponseEntity.ok(transporterService.saveInput(request));
    }

    @PostMapping("/assignment")
    public ResponseEntity<AssignmentResponse> getAssignment(@Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(transporterService.getAssignment(request));
    }
}
