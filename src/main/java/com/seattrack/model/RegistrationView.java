package com.seattrack.model;

import java.time.Instant;

public record RegistrationView(
        long registrationId,
        String studentName,
        String studentEmail,
        long workshopId,
        String workshopTitle,
        Instant registeredAt) {

    public long getRegistrationId() {
        return registrationId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public long getWorkshopId() {
        return workshopId;
    }

    public String getWorkshopTitle() {
        return workshopTitle;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }
}
