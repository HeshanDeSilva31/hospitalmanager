package com.example.hospitalmanager.dto;

import com.example.hospitalmanager.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {

    private final Long id;
    private final String username;
    private final String name;
    private final Role role;
}
