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
    
    // CSV 파일에서 추가된 필드들
    private String vehicleName; // 차량명
    private String status; // 차량 상태 (FAST, SLOW 등)
    private String roadCondition; // 도로 상태 (normal, intersection, curve 등)
    private String trafficCondition; // 교통 상태 (normal, heavy 등)
    private String routeName; // 경로명
}
