package com.example.hospitalmanager.service;

import com.example.hospitalmanager.dto.AppointmentRequest;
import com.example.hospitalmanager.dto.AppointmentResponse;
import com.example.hospitalmanager.dto.StatusRequest;
import com.example.hospitalmanager.exception.ApiException;
import com.example.hospitalmanager.model.AppUser;
import com.example.hospitalmanager.model.Appointment;
import com.example.hospitalmanager.model.AppointmentStatus;
import com.example.hospitalmanager.model.Patient;
import com.example.hospitalmanager.model.Role;
import com.example.hospitalmanager.repository.AppointmentRepository;
import com.example.hospitalmanager.repository.PatientRepository;
import com.example.hospitalmanager.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AppointmentResponse bookForPatient(String username, AppointmentRequest request) {
        Patient patient = patientRepository.findByUser_Username(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient profile was not found."));
        AppUser doctor = userRepository.findById(java.util.Objects.requireNonNull(request.getDoctorId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor was not found."));
        if (doctor.getRole() != Role.DOCTOR) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "The selected user is not a doctor.");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listForDoctor(String username) {
        AppUser user = getUser(username);
        List<Appointment> appointments = user.getRole() == Role.ADMIN
                ? appointmentRepository.findAll()
                : appointmentRepository.findAllByDoctor_Id(user.getId());
        return appointments.stream().map(AppointmentService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listForPatient(String username) {
        return appointmentRepository.findAllByPatient_User_Username(username).stream()
                .map(AppointmentService::toResponse)
                .toList();
    }

    @Transactional
    public AppointmentResponse updateStatus(
            @NonNull Long appointmentId, String username, StatusRequest request) {
        AppUser actor = getUser(username);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment was not found."));
        if (actor.getRole() != Role.ADMIN
                && (actor.getRole() != Role.DOCTOR
                || !appointment.getDoctor().getId().equals(actor.getId()))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot update this appointment.");
        }
        appointment.setStatus(request.getStatus());
        return toResponse(appointmentRepository.save(appointment));
    }

    private AppUser getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found."));
    }

    private static AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getName(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getName(),
                appointment.getAppointmentDate(),
                appointment.getStatus());
    }
}
