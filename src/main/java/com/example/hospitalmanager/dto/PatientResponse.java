package com.example.hospitalmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PatientResponse {

    private final Long id;
    private final String name;
    private final String email;
    private final String phone;
    private final String address;
    private final int age;
}
