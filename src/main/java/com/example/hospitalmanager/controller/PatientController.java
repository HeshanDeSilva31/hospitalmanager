package com.example.hospitalmanager.controller;

import com.example.hospitalmanager.dto.AppointmentRequest;
import com.example.hospitalmanager.dto.AppointmentResponse;
import com.example.hospitalmanager.dto.MedicalRecordResponse;
import com.example.hospitalmanager.service.AppointmentService;
import com.example.hospitalmanager.service.MedicalRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@PreAuthorize("hasRole('PATIENT')")
public class PatientController {

    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;

    public PatientController(
            AppointmentService appointmentService,
            MedicalRecordService medicalRecordService) {
        this.appointmentService = appointmentService;
        this.medicalRecordService = medicalRecordService;
    }

    @PostMapping("/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse bookAppointment(
            Authentication authentication,
            @Valid @RequestBody AppointmentRequest request) {
        return appointmentService.bookForPatient(authentication.getName(), request);
    }

    @GetMapping("/appointments")
    public List<AppointmentResponse> listAppointments(Authentication authentication) {
        return appointmentService.listForPatient(authentication.getName());
    }

    @GetMapping("/medical-records")
    public List<MedicalRecordResponse> listMedicalRecords(Authentication authentication) {
        return medicalRecordService.listForPatient(authentication.getName());
    }
}
