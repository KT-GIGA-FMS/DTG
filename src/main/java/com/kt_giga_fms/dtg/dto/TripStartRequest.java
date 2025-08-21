package com.kt_giga_fms.dtg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TripStartRequest {
    
    @NotBlank(message = "차량 ID는 필수입니다")
    private String vehicleId;
    
    @NotBlank(message = "차량 번호판은 필수입니다")
    private String plateNo;
    
    @NotBlank(message = "운전자 ID는 필수입니다")
    private String driverId;
    
    @NotNull(message = "시작 위도는 필수입니다")
    private Double startLatitude;
    
    @NotNull(message = "시작 경도는 필수입니다")
    private Double startLongitude;
    
    private String destination;
    private String purpose;
}
