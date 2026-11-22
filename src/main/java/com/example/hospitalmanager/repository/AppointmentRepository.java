
package com.example.hospitalmanager.repository;

import com.example.hospitalmanager.model.Appointment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByDoctor_Id(Long doctorId);

    List<Appointment> findAllByPatient_User_Username(String username);

    boolean existsByPatient_IdAndDoctor_Id(Long patientId, Long doctorId);

    boolean existsByPatient_Id(Long patientId);
}
