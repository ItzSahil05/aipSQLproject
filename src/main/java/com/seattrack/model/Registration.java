package com.seattrack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "REGISTRATIONS")
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REG_ID")
    private Long registrationId;

    @Column(name = "STUDENT_NAME", nullable = false, length = 150)
    private String studentName;

    @Column(name = "STUDENT_EMAIL", nullable = false, length = 254)
    private String studentEmail;

    @Column(name = "REGISTERED_AT", nullable = false)
    private Instant registeredAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "WORKSHOP_ID", nullable = false)
    private Workshop workshop;

    protected Registration() {
    }

    public Long getRegistrationId() {
        return registrationId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public Workshop getWorkshop() {
        return workshop;
    }
}
