package com.example.hospitalmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicalRecordRequest {

    @NotNull
    @Positive
    private Long patientId;

    @NotBlank
    @Size(max = 2000)
    private String diagnosis;

    @NotBlank
    @Size(max = 2000)
    private String prescription;
}
