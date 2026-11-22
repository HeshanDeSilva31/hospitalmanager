package com.example.hospitalmanager.service;

import com.example.hospitalmanager.dto.AuthResponse;
import com.example.hospitalmanager.dto.LoginRequest;
import com.example.hospitalmanager.dto.RegisterRequest;
import com.example.hospitalmanager.dto.StaffCreateRequest;
import com.example.hospitalmanager.dto.UserResponse;
import com.example.hospitalmanager.exception.ApiException;
import com.example.hospitalmanager.model.AppUser;
import com.example.hospitalmanager.model.Patient;
import com.example.hospitalmanager.model.Role;
import com.example.hospitalmanager.repository.PatientRepository;
import com.example.hospitalmanager.repository.UserRepository;
import com.example.hospitalmanager.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PatientRepository patientRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username is already registered.");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && patientRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered.");
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(Role.PATIENT);
        AppUser savedUser = userRepository.save(user);

        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setEmail(blankToNull(request.getEmail()));
        patient.setPhone(blankToNull(request.getPhone()));
        patient.setAddress(blankToNull(request.getAddress()));
        patient.setAge(request.getAge());
        patient.setUser(savedUser);
        patientRepository.save(patient);

        return toUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AppUser user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password."));
        return new AuthResponse(
                jwtService.generateToken(userDetails),
                "Bearer",
                jwtService.getExpirationSeconds(),
                toUserResponse(user));
    }

    @Transactional
    public UserResponse createStaff(StaffCreateRequest request, Role role) {
        if (role != Role.DOCTOR && role != Role.NURSE && role != Role.RECEPTIONIST) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only doctor, nurse, or receptionist accounts can be created here.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username is already registered.");
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(role);
        return toUserResponse(userRepository.save(user));
    }

    public static UserResponse toUserResponse(AppUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getName(), user.getRole());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
