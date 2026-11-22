package com.example.hospitalmanager.service;

import com.example.hospitalmanager.dto.PatientResponse;
import com.example.hospitalmanager.exception.ApiException;
import com.example.hospitalmanager.model.Patient;
import com.example.hospitalmanager.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> listPatients() {
        return patientRepository.findAll().stream()
                .map(PatientService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Patient getPatient(@NonNull Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient was not found."));
    }

    public static PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(), patient.getName(), patient.getEmail(),
                patient.getPhone(), patient.getAddress(), patient.getAge());
    }
}
