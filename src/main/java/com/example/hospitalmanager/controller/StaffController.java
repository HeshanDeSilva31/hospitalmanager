package com.example.hospitalmanager.controller;

import com.example.hospitalmanager.dto.PatientResponse;
import com.example.hospitalmanager.dto.StaffCreateRequest;
import com.example.hospitalmanager.dto.UserResponse;
import com.example.hospitalmanager.model.Role;
import com.example.hospitalmanager.service.AuthService;
import com.example.hospitalmanager.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class StaffController {

    private final AuthService authService;
    private final PatientService patientService;

    public StaffController(AuthService authService, PatientService patientService) {
        this.authService = authService;
        this.patientService = patientService;
    }

    @GetMapping("/patients")
    public List<PatientResponse> listPatients() {
        return patientService.listPatients();
    }

    @PostMapping("/doctors")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createDoctor(@Valid @RequestBody StaffCreateRequest request) {
        return authService.createStaff(request, Role.DOCTOR);
    }

    @PostMapping("/nurses")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createNurse(@Valid @RequestBody StaffCreateRequest request) {
        return authService.createStaff(request, Role.NURSE);
    }

    @PostMapping("/receptionists")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createReceptionist(@Valid @RequestBody StaffCreateRequest request) {
        return authService.createStaff(request, Role.RECEPTIONIST);
    }
}
