package com.kt_giga_fms.dtg.controller;

import com.kt_giga_fms.dtg.dto.ApiResponse;
import com.kt_giga_fms.dtg.dto.TripStartRequest;
import com.kt_giga_fms.dtg.dto.TripEndRequest;
import com.kt_giga_fms.dtg.service.DtgTrackingService;
import com.kt_giga_fms.dtg.service.TripSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/dtg")
@RequiredArgsConstructor
public class DtgController {
    
    private final DtgTrackingService dtgTrackingService;
    
    /**
     * 운행 시작
     */
    @PostMapping("/trips/start")
    public ResponseEntity<ApiResponse<String>> startTrip(@Valid @RequestBody TripStartRequest request) {
        log.info("운행 시작 요청: 차량={}, 운전자={}", request.getVehicleId(), request.getDriverId());
        
        try {
            String tripId = dtgTrackingService.startTrip(request);
            return ResponseEntity.ok(ApiResponse.success("운행이 시작되었습니다.", tripId));
        } catch (Exception e) {
            log.error("운행 시작 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("운행 시작에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 운행 종료
     */
    @PostMapping("/trips/end")
    public ResponseEntity<ApiResponse<String>> endTrip(@Valid @RequestBody TripEndRequest request) {
        log.info("운행 종료 요청: 차량={}", request.getVehicleId());
        
        try {
            dtgTrackingService.endTrip(request);
            return ResponseEntity.ok(ApiResponse.success("운행이 종료되었습니다."));
        } catch (Exception e) {
            log.error("운행 종료 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("운행 종료에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 활성 운행 목록 조회
     */
    @GetMapping("/trips/active")
    public ResponseEntity<ApiResponse<Map<String, TripSession>>> getActiveTrips() {
        log.info("활성 운행 목록 조회");
        
        try {
            Map<String, TripSession> activeTrips = dtgTrackingService.getActiveTrips();
            return ResponseEntity.ok(ApiResponse.success(activeTrips));
        } catch (Exception e) {
            log.error("활성 운행 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("활성 운행 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 특정 차량의 운행 상태 조회
     */
    @GetMapping("/trips/{vehicleId}")
    public ResponseEntity<ApiResponse<TripSession>> getTripStatus(@PathVariable String vehicleId) {
        log.info("차량 운행 상태 조회: {}", vehicleId);
        
        try {
            TripSession session = dtgTrackingService.getTripSession(vehicleId);
            if (session != null) {
                return ResponseEntity.ok(ApiResponse.success(session));
            } else {
                return ResponseEntity.ok(ApiResponse.success("해당 차량의 활성 운행이 없습니다.", null));
            }
        } catch (Exception e) {
            log.error("차량 운행 상태 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("차량 운행 상태 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * DTG 서비스 상태 확인
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> getHealth() {
        return ResponseEntity.ok(ApiResponse.success("DTG 서비스가 정상적으로 실행 중입니다."));
    }
}
