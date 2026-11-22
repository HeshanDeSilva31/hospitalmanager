package com.example.hospitalmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MedicalRecordResponse {

    private final Long id;
    private final Long patientId;
    private final String patientName;
    private final Long doctorId;
    private final String doctorName;
    private final String diagnosis;
    private final String prescription;
    private final LocalDateTime createdAt;
}
