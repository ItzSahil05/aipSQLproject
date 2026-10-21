package com.seattrack.model;

public record WorkshopView(
        long workshopId,
        String department,
        String title,
        String description,
        int totalSeats,
        int availableSeats) {

    public long getWorkshopId() {
        return workshopId;
    }

    public String getDepartment() {
        return department;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }
}
