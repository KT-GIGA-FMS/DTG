package com.kt_giga_fms.dtg.service;

import com.kt_giga_fms.dtg.config.CsvDataConfig;
import com.kt_giga_fms.dtg.dto.CsvTrackingData;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvDataService {
    
    private final CsvDataConfig csvDataConfig;
    private final Map<String, List<CsvTrackingData>> csvDataMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> vehicleIndexMap = new ConcurrentHashMap<>();
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * CSV 파일들을 로드하고 초기화
     */
    public void initializeCsvData() {
        if (!csvDataConfig.isEnabled()) {
            log.info("CSV 데이터가 비활성화되어 있습니다.");
            return;
        }
        
        log.info("CSV 데이터 초기화 시작...");
        
        List<String> csvFiles = csvDataConfig.getCsvFiles();
        if (csvFiles == null || csvFiles.isEmpty()) {
            log.warn("설정된 CSV 파일이 없습니다.");
            return;
        }
        
        for (String csvFile : csvFiles) {
            try {
                String trimmedFile = csvFile.trim();
                if (!trimmedFile.isEmpty()) {
                    List<CsvTrackingData> dataList = loadCsvFile(trimmedFile);
                    if (!dataList.isEmpty()) {
                        String vehicleId = dataList.get(0).getVehicleId();
                        csvDataMap.put(vehicleId, dataList);
                        vehicleIndexMap.put(vehicleId, 0);
                        log.info("CSV 파일 로드 완료: {} (차량: {}, 데이터: {}개)", 
                                trimmedFile, vehicleId, dataList.size());
                    }
                }
            } catch (Exception e) {
                log.error("CSV 파일 로드 실패: {}", csvFile, e);
            }
        }
        
        log.info("CSV 데이터 초기화 완료. 총 {}개 차량 데이터 로드됨", csvDataMap.size());
    }
    
    /**
     * CSV 파일 로드
     */
    private List<CsvTrackingData> loadCsvFile(String csvFile) throws IOException {
        ClassPathResource resource = new ClassPathResource(csvFile);
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {
            HeaderColumnNameMappingStrategy<CsvTrackingData> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(CsvTrackingData.class);
            
            CsvToBean<CsvTrackingData> csvToBean = new CsvToBeanBuilder<CsvTrackingData>(reader)
                    .withMappingStrategy(strategy)
                    .withSkipLines(0)
                    .build();
            
            List<CsvTrackingData> dataList = csvToBean.parse();
            
            // timestamp 파싱
            for (CsvTrackingData data : dataList) {
                if (data.getTimestamp() == null && data.getTimestamp() != null) {
                    try {
                        LocalDateTime parsedTime = LocalDateTime.parse(data.getTimestamp().toString(), formatter);
                        data.setTimestamp(parsedTime);
                    } catch (Exception e) {
                        log.warn("Timestamp 파싱 실패: {}", data.getTimestamp());
                    }
                }
            }
            
            return dataList;
        }
    }
    
    /**
     * 차량별 다음 추적 데이터 조회
     */
    public CsvTrackingData getNextTrackingData(String vehicleId) {
        List<CsvTrackingData> dataList = csvDataMap.get(vehicleId);
        if (dataList == null || dataList.isEmpty()) {
            log.warn("차량 {}의 CSV 데이터를 찾을 수 없음", vehicleId);
            return null;
        }
        
        Integer currentIndex = vehicleIndexMap.get(vehicleId);
        if (currentIndex == null) {
            currentIndex = 0;
        }
        
        // 인덱스가 데이터 크기를 초과하면 처음부터 다시 시작
        if (currentIndex >= dataList.size()) {
            currentIndex = 0;
        }
        
        CsvTrackingData data = dataList.get(currentIndex);
        
        // 다음 인덱스로 업데이트
        vehicleIndexMap.put(vehicleId, currentIndex + 1);
        
        return data;
    }
    
    /**
     * 차량별 CSV 데이터 재시작
     */
    public void resetVehicleData(String vehicleId) {
        vehicleIndexMap.put(vehicleId, 0);
        log.info("차량 {}의 CSV 데이터 인덱스 재설정", vehicleId);
    }
    
    /**
     * 사용 가능한 차량 ID 목록 조회
     */
    public Set<String> getAvailableVehicleIds() {
        return new HashSet<>(csvDataMap.keySet());
    }
    
    /**
     * 차량별 데이터 개수 조회
     */
    public int getDataCount(String vehicleId) {
        List<CsvTrackingData> dataList = csvDataMap.get(vehicleId);
        return dataList != null ? dataList.size() : 0;
    }
    
    /**
     * 차량별 현재 인덱스 조회
     */
    public int getCurrentIndex(String vehicleId) {
        return vehicleIndexMap.getOrDefault(vehicleId, 0);
    }
    
    /**
     * CSV 데이터 활성화 여부 확인
     */
    public boolean isEnabled() {
        return csvDataConfig.isEnabled();
    }
}
