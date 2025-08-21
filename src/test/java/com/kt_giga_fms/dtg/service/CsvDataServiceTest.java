package com.kt_giga_fms.dtg.service;

import com.kt_giga_fms.dtg.config.CsvDataConfig;
import com.kt_giga_fms.dtg.dto.CsvTrackingData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvDataServiceTest {

    @Mock
    private CsvDataConfig csvDataConfig;

    private CsvDataService csvDataService;

    @BeforeEach
    void setUp() {
        csvDataService = new CsvDataService(csvDataConfig);
    }

    @Test
    void testCsvFileLoading() throws IOException {
        // Given
        when(csvDataConfig.isEnabled()).thenReturn(true);
        when(csvDataConfig.getCsvFiles()).thenReturn(Arrays.asList("data/광화문광장_vehicle_data.csv"));

        // When
        csvDataService.initializeCsvData();

        // Then
        assertTrue(csvDataService.isEnabled());
        assertEquals(1, csvDataService.getAvailableVehicleIds().size());
        assertTrue(csvDataService.getAvailableVehicleIds().contains("veh-0011"));
        assertEquals(28, csvDataService.getDataCount("veh-0011")); // 실제 데이터 개수로 수정
    }

    @Test
    void testCsvDataRetrieval() throws IOException {
        // Given
        when(csvDataConfig.isEnabled()).thenReturn(true);
        when(csvDataConfig.getCsvFiles()).thenReturn(Arrays.asList("data/광화문광장_vehicle_data.csv"));
        csvDataService.initializeCsvData();

        // When
        CsvTrackingData firstData = csvDataService.getNextTrackingData("veh-0011");
        CsvTrackingData secondData = csvDataService.getNextTrackingData("veh-0011");

        // Then
        assertNotNull(firstData);
        assertEquals("veh-0011", firstData.getVehicleId());
        assertEquals("Vehicle_0011", firstData.getVehicleName());
        assertEquals(37.5727157, firstData.getLatitude());
        assertEquals(126.9784059, firstData.getLongitude());
        assertEquals(52.85, firstData.getSpeed());
        assertEquals(270.39, firstData.getHeading());
        assertEquals("FAST", firstData.getStatus());
        assertEquals("RUNNING", firstData.getEngineStatus());
        assertEquals("intersection", firstData.getRoadCondition());
        assertEquals("heavy", firstData.getTrafficCondition());
        assertEquals("광화문광장", firstData.getRouteName());

        assertNotNull(secondData);
        assertEquals("veh-0011", secondData.getVehicleId());
        assertEquals(37.5727167, secondData.getLatitude());
        assertEquals(126.9782284, secondData.getLongitude());
    }

    @Test
    void testCsvDataExhaustion() throws IOException {
        // Given
        when(csvDataConfig.isEnabled()).thenReturn(true);
        when(csvDataConfig.getCsvFiles()).thenReturn(Arrays.asList("data/광화문광장_vehicle_data.csv"));
        csvDataService.initializeCsvData();

        // When - 모든 데이터를 읽어서 소진시킴
        for (int i = 0; i < 28; i++) { // 실제 데이터 개수로 수정
            csvDataService.getNextTrackingData("veh-0011");
        }

        // Then
        assertTrue(csvDataService.isCsvDataExhausted("veh-0011"));
        assertNull(csvDataService.getNextTrackingData("veh-0011"));
    }

    @Test
    void testCsvDataReset() throws IOException {
        // Given
        when(csvDataConfig.isEnabled()).thenReturn(true);
        when(csvDataConfig.getCsvFiles()).thenReturn(Arrays.asList("data/광화문광장_vehicle_data.csv"));
        csvDataService.initializeCsvData();

        // When - 몇 개 데이터를 읽고 리셋
        csvDataService.getNextTrackingData("veh-0011");
        csvDataService.getNextTrackingData("veh-0011");
        csvDataService.resetVehicleData("veh-0011");

        // Then
        assertEquals(0, csvDataService.getCurrentIndex("veh-0011"));
        CsvTrackingData firstData = csvDataService.getNextTrackingData("veh-0011");
        assertEquals(37.5727157, firstData.getLatitude()); // 첫 번째 데이터와 동일
    }

    @Test
    void testCsvDataWithDifferentFiles() throws IOException {
        // Given
        when(csvDataConfig.isEnabled()).thenReturn(true);
        when(csvDataConfig.getCsvFiles()).thenReturn(Arrays.asList(
            "data/광화문광장_vehicle_data.csv",
            "data/종묘_vehicle_data.csv"
        ));

        // When
        csvDataService.initializeCsvData();

        // Then
        assertTrue(csvDataService.getAvailableVehicleIds().size() >= 2);
        // 각 파일의 데이터 개수 확인
        assertTrue(csvDataService.getDataCount("veh-0011") > 0);
    }

    @Test
    void testCsvDataDisabled() {
        // Given
        when(csvDataConfig.isEnabled()).thenReturn(false);

        // When
        csvDataService.initializeCsvData();

        // Then
        assertFalse(csvDataService.isEnabled());
        assertEquals(0, csvDataService.getAvailableVehicleIds().size());
    }

    @Test
    void testCsvDataWithEmptyFiles() {
        // Given
        when(csvDataConfig.isEnabled()).thenReturn(true);
        when(csvDataConfig.getCsvFiles()).thenReturn(Arrays.asList());

        // When
        csvDataService.initializeCsvData();

        // Then
        assertEquals(0, csvDataService.getAvailableVehicleIds().size());
    }

    @Test
    void testCsvDataWithNullFiles() {
        // Given
        when(csvDataConfig.isEnabled()).thenReturn(true);
        when(csvDataConfig.getCsvFiles()).thenReturn(null);

        // When
        csvDataService.initializeCsvData();

        // Then
        assertEquals(0, csvDataService.getAvailableVehicleIds().size());
    }
}
