package com.example.hospitalmanager.service;

import com.example.hospitalmanager.dto.MedicalRecordRequest;
import com.example.hospitalmanager.dto.MedicalRecordResponse;
import com.example.hospitalmanager.exception.ApiException;
import com.example.hospitalmanager.model.AppUser;
import com.example.hospitalmanager.model.MedicalRecord;
import com.example.hospitalmanager.model.Patient;
import com.example.hospitalmanager.model.Role;
import com.example.hospitalmanager.repository.AppointmentRepository;
import com.example.hospitalmanager.repository.MedicalRecordRepository;
import com.example.hospitalmanager.repository.PatientRepository;
import com.example.hospitalmanager.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    public MedicalRecordService(
            MedicalRecordRepository medicalRecordRepository,
            PatientRepository patientRepository,
            UserRepository userRepository,
            AppointmentRepository appointmentRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public MedicalRecordResponse create(String username, MedicalRecordRequest request) {
        AppUser doctor = getUser(username);
        if (doctor.getRole() != Role.DOCTOR) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only a doctor can create a medical record.");
        }
        Patient patient = patientRepository.findById(java.util.Objects.requireNonNull(request.getPatientId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient was not found."));
        if (!appointmentRepository.existsByPatient_IdAndDoctor_Id(patient.getId(), doctor.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "A medical record requires an appointment with this doctor.");
        }

        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setDiagnosis(request.getDiagnosis());
        record.setPrescription(request.getPrescription());
        record.setCreatedAt(LocalDateTime.now());
        return toResponse(medicalRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> listForPatient(String username) {
        if (getUser(username).getRole() != Role.PATIENT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only a patient can view this resource.");
        }
        return medicalRecordRepository.findAllByPatient_User_Username(username).stream()
                .map(MedicalRecordService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> listForPatientByStaff(
            @NonNull Long patientId, String username) {
        AppUser actor = getUser(username);
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient was not found."));
        if (actor.getRole() != Role.ADMIN
                && (actor.getRole() != Role.DOCTOR
                || !appointmentRepository.existsByPatient_IdAndDoctor_Id(patientId, actor.getId()))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot view this patient's records.");
        }
        return medicalRecordRepository.findAllByPatient_Id(patient.getId()).stream()
                .map(MedicalRecordService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> listAll() {
        return medicalRecordRepository.findAll().stream()
                .map(MedicalRecordService::toResponse)
                .toList();
    }

    private AppUser getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found."));
    }

    private static MedicalRecordResponse toResponse(MedicalRecord record) {
        return new MedicalRecordResponse(
                record.getId(),
                record.getPatient().getId(),
                record.getPatient().getName(),
                record.getDoctor().getId(),
                record.getDoctor().getName(),
                record.getDiagnosis(),
                record.getPrescription(),
                record.getCreatedAt());
    }
}
