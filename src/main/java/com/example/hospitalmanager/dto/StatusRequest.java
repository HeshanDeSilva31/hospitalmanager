package com.example.hospitalmanager.dto;

import com.example.hospitalmanager.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusRequest {

    @NotNull
    private AppointmentStatus status;
}
