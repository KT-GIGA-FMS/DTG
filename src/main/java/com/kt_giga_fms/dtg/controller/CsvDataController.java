package com.kt_giga_fms.dtg.controller;

import com.kt_giga_fms.dtg.service.CsvDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/v1/dtg/csv")
@RequiredArgsConstructor
public class CsvDataController {

    private final CsvDataService csvDataService;

    /**
     * CSV 데이터 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCsvDataStatus() {
        boolean enabled = csvDataService.isEnabled();
        Set<String> availableVehicleIds = csvDataService.getAvailableVehicleIds();
        
        Map<String, Object> status = Map.of(
            "enabled", enabled,
            "availableVehicleIds", availableVehicleIds,
            "totalVehicles", availableVehicleIds.size()
        );
        
        return ResponseEntity.ok(status);
    }

    /**
     * 특정 차량의 CSV 데이터 정보 조회
     */
    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<Map<String, Object>> getVehicleCsvDataInfo(@PathVariable String vehicleId) {
        if (!csvDataService.getAvailableVehicleIds().contains(vehicleId)) {
            return ResponseEntity.notFound().build();
        }
        
        int dataCount = csvDataService.getDataCount(vehicleId);
        int currentIndex = csvDataService.getCurrentIndex(vehicleId);
        boolean isExhausted = csvDataService.isCsvDataExhausted(vehicleId);
        
        Map<String, Object> info = Map.of(
            "vehicleId", vehicleId,
            "dataCount", dataCount,
            "currentIndex", currentIndex,
            "isExhausted", isExhausted,
            "remainingData", Math.max(0, dataCount - currentIndex)
        );
        
        return ResponseEntity.ok(info);
    }

    /**
     * 특정 차량의 CSV 데이터 재시작
     */
    @PostMapping("/vehicles/{vehicleId}/reset")
    public ResponseEntity<Map<String, Object>> resetVehicleCsvData(@PathVariable String vehicleId) {
        if (!csvDataService.getAvailableVehicleIds().contains(vehicleId)) {
            return ResponseEntity.notFound().build();
        }
        
        csvDataService.resetVehicleData(vehicleId);
        
        Map<String, Object> result = Map.of(
            "message", "차량 " + vehicleId + "의 CSV 데이터가 재시작되었습니다.",
            "vehicleId", vehicleId,
            "currentIndex", csvDataService.getCurrentIndex(vehicleId)
        );
        
        return ResponseEntity.ok(result);
    }

    /**
     * CSV 데이터 재초기화
     */
    @PostMapping("/reinitialize")
    public ResponseEntity<Map<String, Object>> reinitializeCsvData() {
        csvDataService.initializeCsvData();
        
        Set<String> availableVehicleIds = csvDataService.getAvailableVehicleIds();
        
        Map<String, Object> result = Map.of(
            "message", "CSV 데이터가 재초기화되었습니다.",
            "totalVehicles", availableVehicleIds.size(),
            "availableVehicleIds", availableVehicleIds
        );
        
        return ResponseEntity.ok(result);
    }
}
