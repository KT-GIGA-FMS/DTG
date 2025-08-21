package com.kt_giga_fms.dtg.service;

import com.kt_giga_fms.dtg.dto.TrackingData;
import com.kt_giga_fms.dtg.dto.TripStartRequest;
import com.kt_giga_fms.dtg.dto.TripEndRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DtgTrackingService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final CarTrackingIntegrationService carTrackingIntegrationService;
    private final CsvDataService csvDataService;
    private final Map<String, TripSession> activeTrips = new ConcurrentHashMap<>();
    
    /**
     * 애플리케이션 시작 시 CSV 데이터 초기화
     */
    @PostConstruct
    public void initialize() {
        csvDataService.initializeCsvData();
    }
    
    /**
     * 운행 시작
     */
    public String startTrip(TripStartRequest request) {
        String tripId = UUID.randomUUID().toString();
        
        TripSession session = new TripSession(
            tripId,
            request.getVehicleId(),
            request.getPlateNo(),
            request.getDriverId(),
            request.getStartLatitude(),
            request.getStartLongitude(),
            request.getDestination(),
            request.getPurpose()
        );
        
        activeTrips.put(request.getVehicleId(), session);
        
        log.info("운행 시작: 차량={}, 운전자={}, 여행ID={}", 
                request.getVehicleId(), request.getDriverId(), tripId);
        
        // car-tracking-service에 운행 시작 알림
        carTrackingIntegrationService.notifyTripStarted(request.getVehicleId(), "TRIP_STARTED", request);
        
        return tripId;
    }
    
    /**
     * 운행 종료
     */
    public void endTrip(TripEndRequest request) {
        TripSession session = activeTrips.remove(request.getVehicleId());
        
        if (session != null) {
            session.setEndTime(LocalDateTime.now());
            session.setEndLatitude(request.getEndLatitude());
            session.setEndLongitude(request.getEndLongitude());
            session.setEndReason(request.getEndReason());
            
            log.info("운행 종료: 차량={}, 여행ID={}", request.getVehicleId(), session.getTripId());
            
            // car-tracking-service에 운행 종료 알림
            carTrackingIntegrationService.notifyTripEnded(request.getVehicleId(), "TRIP_ENDED", request);
            
            // 운행 완료 데이터를 analytics-service로 전송
            sendTripCompletionData(session);
        } else {
            log.warn("활성 운행을 찾을 수 없음: 차량={}", request.getVehicleId());
        }
    }
    
    /**
     * 1초마다 실시간 추적 데이터 전송
     */
    @Scheduled(fixedRate = 1000)
    public void sendTrackingData() {
        activeTrips.values().forEach(session -> {
            if (session.isActive()) {
                TrackingData trackingData = generateTrackingData(session);
                
                if (trackingData != null) {
                    // car-tracking-service로 데이터 전송
                    carTrackingIntegrationService.sendTrackingData(trackingData);
                    
                    // WebSocket으로 프론트엔드에 데이터 전송
                    sendToFrontend(trackingData);
                } else {
                    // CSV 데이터가 끝에 도달했으면 운행 자동 종료
                    log.info("차량 {}의 CSV 데이터가 모두 소진되어 운행을 자동 종료합니다.", session.getVehicleId());
                    autoEndTrip(session);
                }
            }
        });
    }
    
    /**
     * CSV 데이터 소진으로 인한 자동 운행 종료
     */
    private void autoEndTrip(TripSession session) {
        TripEndRequest endRequest = new TripEndRequest();
        endRequest.setVehicleId(session.getVehicleId());
        endRequest.setEndLatitude(session.getStartLatitude()); // 시작 위치로 설정
        endRequest.setEndLongitude(session.getStartLongitude());
        endRequest.setEndReason("CSV 데이터 소진으로 인한 자동 종료");
        
        endTrip(endRequest);
    }
    
    /**
     * 추적 데이터 생성 (CSV 기반)
     */
    private TrackingData generateTrackingData(TripSession session) {
        // CSV 데이터가 활성화되어 있지 않으면 랜덤 데이터 생성 (fallback)
        if (!csvDataService.isEnabled()) {
            return generateRandomTrackingData(session);
        }
        
        // CSV에서 다음 추적 데이터 조회
        TrackingData csvData = csvDataService.getNextTrackingDataAsTrackingData(
            session.getVehicleId(), 
            session.getPlateNo(), 
            session.getTripId()
        );
        
        if (csvData != null) {
            log.debug("CSV 데이터 사용: 차량={}, 위치=({}, {}), 속도={}km/h", 
                    session.getVehicleId(), 
                    csvData.getLatitude(), 
                    csvData.getLongitude(), 
                    csvData.getSpeed());
            return csvData;
        } else {
            log.warn("차량 {}의 CSV 데이터를 찾을 수 없어 랜덤 데이터 사용", session.getVehicleId());
            return generateRandomTrackingData(session);
        }
    }
    
    /**
     * 랜덤 추적 데이터 생성 (fallback)
     */
    private TrackingData generateRandomTrackingData(TripSession session) {
        TrackingData data = new TrackingData();
        data.setVehicleId(session.getVehicleId());
        data.setPlateNo(session.getPlateNo());
        data.setTripId(session.getTripId());
        data.setTimestamp(LocalDateTime.now());
        
        // 실제 DTG 장치에서는 실제 GPS 데이터를 받아옴
        // 여기서는 시뮬레이션 데이터 생성
        if (session.getStartLatitude() != null && session.getStartLongitude() != null) {
            // 간단한 이동 시뮬레이션
            double latOffset = (Math.random() - 0.5) * 0.001; // 약 100m
            double lngOffset = (Math.random() - 0.5) * 0.001;
            
            data.setLatitude(session.getStartLatitude() + latOffset);
            data.setLongitude(session.getStartLongitude() + lngOffset);
            data.setSpeed(30.0 + Math.random() * 40.0); // 30-70 km/h
            data.setHeading(Math.random() * 360.0);
            data.setAltitude(50.0 + Math.random() * 20.0);
            data.setFuelLevel(80.0 + Math.random() * 20.0);
            data.setEngineStatus("ON");
        }
        
        return data;
    }
    
    /**
     * 프론트엔드로 WebSocket 데이터 전송
     */
    private void sendToFrontend(TrackingData data) {
        try {
            String destination = "/topic/tracking/" + data.getVehicleId();
            messagingTemplate.convertAndSend(destination, data);
        } catch (Exception e) {
            log.error("프론트엔드 데이터 전송 실패: {}", e.getMessage());
        }
    }
    
    /**
     * analytics-service로 운행 완료 데이터 전송
     */
    private void sendTripCompletionData(TripSession session) {
        try {
            // REST API로 analytics-service에 운행 완료 데이터 전송
            log.info("analytics-service로 운행 완료 데이터 전송: 차량={}", session.getVehicleId());
        } catch (Exception e) {
            log.error("analytics-service 데이터 전송 실패: {}", e.getMessage());
        }
    }
    
    /**
     * 활성 운행 세션 조회
     */
    public Map<String, TripSession> getActiveTrips() {
        return new ConcurrentHashMap<>(activeTrips);
    }
    
    /**
     * 특정 차량의 운행 상태 조회
     */
    public TripSession getTripSession(String vehicleId) {
        return activeTrips.get(vehicleId);
    }
}
