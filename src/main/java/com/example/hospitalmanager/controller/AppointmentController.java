package com.example.hospitalmanager.controller;

import com.example.hospitalmanager.dto.AppointmentResponse;
import com.example.hospitalmanager.dto.MedicalRecordRequest;
import com.example.hospitalmanager.dto.MedicalRecordResponse;
import com.example.hospitalmanager.dto.StatusRequest;
import com.example.hospitalmanager.service.AppointmentService;
import com.example.hospitalmanager.service.MedicalRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;

    public AppointmentController(
            AppointmentService appointmentService,
            MedicalRecordService medicalRecordService) {
        this.appointmentService = appointmentService;
        this.medicalRecordService = medicalRecordService;
    }

    @GetMapping("/api/doctors/appointments")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public List<AppointmentResponse> listDoctorAppointments(Authentication authentication) {
        return appointmentService.listForDoctor(authentication.getName());
    }

    @PatchMapping("/api/appointments/{appointmentId}/status")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public AppointmentResponse updateAppointmentStatus(
            @PathVariable @NonNull Long appointmentId,
            Authentication authentication,
            @Valid @RequestBody StatusRequest request) {
        return appointmentService.updateStatus(appointmentId, authentication.getName(), request);
    }

    @PostMapping("/api/doctors/medical-records")
    @PreAuthorize("hasRole('DOCTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public MedicalRecordResponse createMedicalRecord(
            Authentication authentication,
            @Valid @RequestBody MedicalRecordRequest request) {
        return medicalRecordService.create(authentication.getName(), request);
    }

    @GetMapping("/api/doctors/patients/{patientId}/medical-records")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public List<MedicalRecordResponse> listPatientRecords(
            @PathVariable @NonNull Long patientId, Authentication authentication) {
        return medicalRecordService.listForPatientByStaff(patientId, authentication.getName());
    }

    @GetMapping("/api/admin/medical-records")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MedicalRecordResponse> listAllRecords() {
        return medicalRecordService.listAll();
    }
}
