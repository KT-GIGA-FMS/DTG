package com.kt_giga_fms.dtg.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CsvTrackingData {
    
    private Long id;
    private String vehicleId;
    private String vehicleName;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double heading;
    private String status;
    private LocalDateTime timestamp;
    private Double fuelLevel;
    private String engineStatus;
    private String roadCondition;
    private String trafficCondition;
    private String routeName;
}
