package com.example.hospitalmanager.dto;

import com.example.hospitalmanager.model.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AppointmentResponse {

    private final Long id;
    private final Long patientId;
    private final String patientName;
    private final Long doctorId;
    private final String doctorName;
    private final LocalDateTime appointmentDate;
    private final AppointmentStatus status;
}
