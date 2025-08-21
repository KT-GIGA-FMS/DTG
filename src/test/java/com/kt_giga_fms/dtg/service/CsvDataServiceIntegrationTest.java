package com.kt_giga_fms.dtg.service;

import com.kt_giga_fms.dtg.config.CsvDataConfig;
import com.kt_giga_fms.dtg.dto.CsvTrackingData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "csv.data.enabled=true",
    "csv.data.files=data/광화문광장_vehicle_data.csv,data/종묘_vehicle_data.csv"
})
class CsvDataServiceIntegrationTest {

    @Autowired
    private CsvDataService csvDataService;

    @Autowired
    private CsvDataConfig csvDataConfig;

    @Test
    void testCsvDataLoading() {
        // Given & When
        csvDataService.initializeCsvData();

        // Then
        assertTrue(csvDataService.isEnabled());
        assertTrue(csvDataService.getAvailableVehicleIds().size() >= 1);
        
        // 광화문광장 데이터 확인
        if (csvDataService.getAvailableVehicleIds().contains("veh-0011")) {
            assertEquals(28, csvDataService.getDataCount("veh-0011"));
            
            // 첫 번째 데이터 확인
            CsvTrackingData firstData = csvDataService.getNextTrackingData("veh-0011");
            assertNotNull(firstData);
            assertEquals("veh-0011", firstData.getVehicleId());
            assertEquals("Vehicle_0011", firstData.getVehicleName());
            assertEquals(37.5727157, firstData.getLatitude());
            assertEquals(126.9784059, firstData.getLongitude());
            assertEquals(52.85, firstData.getSpeed());
            assertEquals("2025-01-15 08:08:00", firstData.getTimestamp());
        }
        
        // 종묘 데이터 확인
        if (csvDataService.getAvailableVehicleIds().contains("veh-0012")) {
            assertTrue(csvDataService.getDataCount("veh-0012") > 0);
        }
    }

    @Test
    void testCsvDataRetrieval() {
        // Given
        csvDataService.initializeCsvData();
        csvDataService.resetVehicleData("veh-0011");

        // When
        CsvTrackingData firstData = csvDataService.getNextTrackingData("veh-0011");
        CsvTrackingData secondData = csvDataService.getNextTrackingData("veh-0011");

        // Then
        assertNotNull(firstData);
        assertEquals("veh-0011", firstData.getVehicleId());
        assertEquals("2025-01-15 08:08:00", firstData.getTimestamp());

        assertNotNull(secondData);
        assertEquals("veh-0011", secondData.getVehicleId());
        assertEquals("2025-01-15 08:08:01", secondData.getTimestamp());
    }

    @Test
    void testCsvDataExhaustion() {
        // Given
        csvDataService.initializeCsvData();
        csvDataService.resetVehicleData("veh-0011");

        // When - 모든 데이터를 읽어서 소진시킴
        for (int i = 0; i < 28; i++) {
            csvDataService.getNextTrackingData("veh-0011");
        }

        // Then
        assertTrue(csvDataService.isCsvDataExhausted("veh-0011"));
        assertNull(csvDataService.getNextTrackingData("veh-0011"));
    }
}
