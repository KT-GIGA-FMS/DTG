package com.kt_giga_fms.dtg.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class CsvTrackingData {
    
    @CsvBindByName(column = "id")
    private Long id;
    
    @CsvBindByName(column = "vehicleId")
    private String vehicleId;
    
    @CsvBindByName(column = "vehicleName")
    private String vehicleName;
    
    @CsvBindByName(column = "latitude")
    private Double latitude;
    
    @CsvBindByName(column = "longitude")
    private Double longitude;
    
    @CsvBindByName(column = "speed")
    private Double speed;
    
    @CsvBindByName(column = "heading")
    private Double heading;
    
    @CsvBindByName(column = "status")
    private String status;
    
    @CsvBindByName(column = "timestamp")
    private String timestamp; // LocalDateTime 대신 String으로 변경
    
    @CsvBindByName(column = "fuelLevel")
    private Double fuelLevel;
    
    @CsvBindByName(column = "engineStatus")
    private String engineStatus;
    
    @CsvBindByName(column = "roadCondition")
    private String roadCondition;
    
    @CsvBindByName(column = "trafficCondition")
    private String trafficCondition;
    
    @CsvBindByName(column = "routeName")
    private String routeName;
}
