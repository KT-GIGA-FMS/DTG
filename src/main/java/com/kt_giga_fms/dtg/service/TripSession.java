package com.kt_giga_fms.dtg.service;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TripSession {
    
    private String tripId;
    private String vehicleId;
    private String plateNo;
    private String driverId;
    private Double startLatitude;
    private Double startLongitude;
    private String destination;
    private String purpose;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double endLatitude;
    private Double endLongitude;
    private String endReason;
    private boolean active;
    
    public TripSession(String tripId, String vehicleId, String plateNo, String driverId,
                      Double startLatitude, Double startLongitude, String destination, String purpose) {
        this.tripId = tripId;
        this.vehicleId = vehicleId;
        this.plateNo = plateNo;
        this.driverId = driverId;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.destination = destination;
        this.purpose = purpose;
        this.startTime = LocalDateTime.now();
        this.active = true;
    }
    
    public boolean isActive() {
        return active && endTime == null;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        this.active = false;
    }
}
