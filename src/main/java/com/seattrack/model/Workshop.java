package com.seattrack.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "WORKSHOPS")
public class Workshop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WORKSHOP_ID")
    private Long workshopId;

    @Column(name = "DEPARTMENT", nullable = false, length = 100)
    private String department;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "TOTAL_SEATS", nullable = false)
    private int totalSeats;

    @Column(name = "AVAILABLE_SEATS", nullable = false)
    private int availableSeats;

    @OneToMany(mappedBy = "workshop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Registration> registrations = new ArrayList<>();

    protected Workshop() {
    }

    public Long getWorkshopId() {
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

    public List<Registration> getRegistrations() {
        return registrations;
    }
}
