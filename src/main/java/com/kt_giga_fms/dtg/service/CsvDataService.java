package com.kt_giga_fms.dtg.service;

import com.kt_giga_fms.dtg.dto.CsvTrackingData;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
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
public class CsvDataService {
    
    private final Map<String, List<CsvTrackingData>> csvDataMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> vehicleIndexMap = new ConcurrentHashMap<>();
    private final List<String> csvFiles = Arrays.asList(
        "data/광화문광장_vehicle_data.csv",
        "data/광화문 (정문)_vehicle_data.csv",
        "data/종묘_vehicle_data.csv",
        "data/창경궁_vehicle_data.csv",
        "data/덕수궁_vehicle_data.csv",
        "data/운현궁_vehicle_data.csv",
        "data/북촌 한옥마을_vehicle_data.csv",
        "data/인사동_vehicle_data.csv",
        "data/관철동 (젊음의 거리)_vehicle_data.csv",
        "data/세운전자상가_vehicle_data.csv",
        "data/청계천 입구_vehicle_data.csv",
        "data/광장시장_vehicle_data.csv",
        "data/귀금속 거리 (종로)_vehicle_data.csv",
        "data/LG 광화문빌딩_vehicle_data.csv",
        "data/조선일보광화문빌딩_vehicle_data.csv",
        "data/판교kt_vehicle_data.csv"
    );
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * CSV 파일들을 로드하고 초기화
     */
    public void initializeCsvData() {
        log.info("CSV 데이터 초기화 시작...");
        
        for (String csvFile : csvFiles) {
            try {
                List<CsvTrackingData> dataList = loadCsvFile(csvFile);
                if (!dataList.isEmpty()) {
                    String vehicleId = dataList.get(0).getVehicleId();
                    csvDataMap.put(vehicleId, dataList);
                    vehicleIndexMap.put(vehicleId, 0);
                    log.info("CSV 파일 로드 완료: {} (차량: {}, 데이터: {}개)", 
                            csvFile, vehicleId, dataList.size());
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
}
