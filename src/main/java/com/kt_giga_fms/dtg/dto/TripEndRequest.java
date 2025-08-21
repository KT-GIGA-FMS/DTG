package com.kt_giga_fms.dtg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TripEndRequest {
    
    @NotBlank(message = "차량 ID는 필수입니다")
    private String vehicleId;
    
    @NotNull(message = "종료 위도는 필수입니다")
    private Double endLatitude;
    
    @NotNull(message = "종료 경도는 필수입니다")
    private Double endLongitude;
    
    private String endReason;
    private String notes;
}
