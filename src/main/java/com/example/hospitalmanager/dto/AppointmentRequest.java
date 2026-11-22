package com.example.hospitalmanager.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AppointmentRequest {

    @NotNull
    @Positive
    private Long doctorId;

    @NotNull
    @Future
    private LocalDateTime appointmentDate;
}
