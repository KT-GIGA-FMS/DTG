package com.kt_giga_fms.dtg.service;

import com.kt_giga_fms.dtg.dto.TrackingData;
import com.kt_giga_fms.dtg.dto.TripStartRequest;
import com.kt_giga_fms.dtg.dto.TripEndRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarTrackingIntegrationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${car.tracking.service.url:http://localhost:8082}")
    private String carTrackingServiceUrl;
    
    /**
     * car-tracking-service에 운행 시작 알림
     */
    public void notifyTripStarted(String vehicleId, String status, TripStartRequest request) {
        try {
            String url = carTrackingServiceUrl + "/api/v1/tracking/trips/start";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<TripStartRequest> entity = new HttpEntity<>(request, headers);
            
            restTemplate.postForObject(url, entity, String.class);
            
            log.info("car-tracking-service에 운행 시작 알림 전송 완료: 차량={}", vehicleId);
        } catch (Exception e) {
            log.error("car-tracking-service 운행 시작 알림 실패: {}", e.getMessage());
        }
    }
    
    /**
     * car-tracking-service에 운행 종료 알림
     */
    public void notifyTripEnded(String vehicleId, String status, TripEndRequest request) {
        try {
            String url = carTrackingServiceUrl + "/api/v1/tracking/trips/end";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<TripEndRequest> entity = new HttpEntity<>(request, headers);
            
            restTemplate.postForObject(url, entity, String.class);
            
            log.info("car-tracking-service에 운행 종료 알림 전송 완료: 차량={}", vehicleId);
        } catch (Exception e) {
            log.error("car-tracking-service 운행 종료 알림 실패: {}", e.getMessage());
        }
    }
    
    /**
     * car-tracking-service에 실시간 추적 데이터 전송
     */
    public void sendTrackingData(TrackingData data) {
        try {
            String url = carTrackingServiceUrl + "/api/v1/tracking/data";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<TrackingData> entity = new HttpEntity<>(data, headers);
            
            restTemplate.postForObject(url, entity, String.class);
            
            log.debug("car-tracking-service에 추적 데이터 전송 완료: 차량={}", data.getVehicleId());
        } catch (Exception e) {
            log.error("car-tracking-service 추적 데이터 전송 실패: {}", e.getMessage());
        }
    }
}
