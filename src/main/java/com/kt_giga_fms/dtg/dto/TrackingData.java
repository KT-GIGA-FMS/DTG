package com.kt_giga_fms.dtg.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TrackingData {
    
    private String vehicleId;
    private String plateNo;
    private Double latitude;
    private Double longitude;
    private Double speed; // km/h
    private Double heading; // 방향 (0-360도)
    private Double altitude; // 고도
    private Double fuelLevel; // 연료 레벨
    private String engineStatus; // 엔진 상태 (ON/OFF)
    private LocalDateTime timestamp;
    private String tripId;
}
