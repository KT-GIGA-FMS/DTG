package com.kt_giga_fms.dtg.service;

import com.kt_giga_fms.dtg.dto.TrackingData;
import com.kt_giga_fms.dtg.dto.TripEndRequest;
import com.kt_giga_fms.dtg.dto.TripStartRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarTrackingIntegrationService 단위 테스트")
class CarTrackingIntegrationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CarTrackingIntegrationService carTrackingIntegrationService;

    private TripStartRequest tripStartRequest;
    private TripEndRequest tripEndRequest;
    private TrackingData trackingData;

    @BeforeEach
    void setUp() {
        // 테스트용 URL 설정
        ReflectionTestUtils.setField(carTrackingIntegrationService, "carTrackingServiceUrl", "http://localhost:8082");

        // 테스트용 TripStartRequest 설정
        tripStartRequest = new TripStartRequest();
        tripStartRequest.setVehicleId("TEST_VEHICLE_001");
        tripStartRequest.setPlateNo("12가3456");
        tripStartRequest.setDriverId("DRIVER_001");
        tripStartRequest.setStartLatitude(37.5665);
        tripStartRequest.setStartLongitude(126.9780);
        tripStartRequest.setDestination("서울시청");
        tripStartRequest.setPurpose("업무");

        // 테스트용 TripEndRequest 설정
        tripEndRequest = new TripEndRequest();
        tripEndRequest.setVehicleId("TEST_VEHICLE_001");
        tripEndRequest.setEndLatitude(37.5665);
        tripEndRequest.setEndLongitude(126.9780);
        tripEndRequest.setEndReason("목적지 도착");

        // 테스트용 TrackingData 설정
        trackingData = new TrackingData();
        trackingData.setVehicleId("TEST_VEHICLE_001");
        trackingData.setPlateNo("12가3456");
        trackingData.setTripId("TRIP_001");
        trackingData.setTimestamp(LocalDateTime.now());
        trackingData.setLatitude(37.5665);
        trackingData.setLongitude(126.9780);
        trackingData.setSpeed(50.0);
        trackingData.setHeading(90.0);
        trackingData.setAltitude(50.0);
        trackingData.setFuelLevel(80.0);
        trackingData.setEngineStatus("ON");
    }

    @Test
    @DisplayName("운행 시작 알림 전송 - 성공")
    void notifyTripStarted_Success() {
        // given
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn("SUCCESS");

        // when
        carTrackingIntegrationService.notifyTripStarted("TEST_VEHICLE_001", "TRIP_STARTED", tripStartRequest);

        // then
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8082/api/v1/tracking/trips/start"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("운행 시작 알림 전송 - 실패 (예외 발생)")
    void notifyTripStarted_Failure() {
        // given
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Network error"));

        // when & then - 예외가 발생해도 로그만 남기고 정상 종료되어야 함
        assertDoesNotThrow(() -> {
            carTrackingIntegrationService.notifyTripStarted("TEST_VEHICLE_001", "TRIP_STARTED", tripStartRequest);
        });

        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8082/api/v1/tracking/trips/start"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("운행 종료 알림 전송 - 성공")
    void notifyTripEnded_Success() {
        // given
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn("SUCCESS");

        // when
        carTrackingIntegrationService.notifyTripEnded("TEST_VEHICLE_001", "TRIP_ENDED", tripEndRequest);

        // then
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8082/api/v1/tracking/trips/end"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("운행 종료 알림 전송 - 실패 (예외 발생)")
    void notifyTripEnded_Failure() {
        // given
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Network error"));

        // when & then - 예외가 발생해도 로그만 남기고 정상 종료되어야 함
        assertDoesNotThrow(() -> {
            carTrackingIntegrationService.notifyTripEnded("TEST_VEHICLE_001", "TRIP_ENDED", tripEndRequest);
        });

        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8082/api/v1/tracking/trips/end"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("실시간 추적 데이터 전송 - 성공")
    void sendTrackingData_Success() {
        // given
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn("SUCCESS");

        // when
        carTrackingIntegrationService.sendTrackingData(trackingData);

        // then
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8082/api/v1/tracking/data"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("실시간 추적 데이터 전송 - 실패 (예외 발생)")
    void sendTrackingData_Failure() {
        // given
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Network error"));

        // when & then - 예외가 발생해도 로그만 남기고 정상 종료되어야 함
        assertDoesNotThrow(() -> {
            carTrackingIntegrationService.sendTrackingData(trackingData);
        });

        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8082/api/v1/tracking/data"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("HTTP 헤더 설정 확인")
    void httpHeadersConfiguration() {
        // given
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn("SUCCESS");

        // when
        carTrackingIntegrationService.notifyTripStarted("TEST_VEHICLE_001", "TRIP_STARTED", tripStartRequest);

        // then
        verify(restTemplate, times(1)).postForObject(
                anyString(),
                argThat(entity -> {
                    HttpEntity<?> httpEntity = (HttpEntity<?>) entity;
                    HttpHeaders headers = httpEntity.getHeaders();
                    
                    // Content-Type이 APPLICATION_JSON으로 설정되었는지 확인
                    return headers.getContentType() != null &&
                           headers.getContentType().toString().contains("application/json");
                }),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("요청 본문 데이터 검증")
    void requestBodyValidation() {
        // given
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn("SUCCESS");

        // when
        carTrackingIntegrationService.notifyTripStarted("TEST_VEHICLE_001", "TRIP_STARTED", tripStartRequest);

        // then
        verify(restTemplate, times(1)).postForObject(
                anyString(),
                argThat(entity -> {
                    HttpEntity<?> httpEntity = (HttpEntity<?>) entity;
                    Object body = httpEntity.getBody();
                    
                    // 요청 본문이 TripStartRequest 타입인지 확인
                    return body instanceof TripStartRequest;
                }),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("다양한 차량 ID에 대한 요청 처리")
    void multipleVehicleIds() {
        // given
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn("SUCCESS");

        TripStartRequest request1 = new TripStartRequest();
        request1.setVehicleId("VEHICLE_001");
        request1.setPlateNo("12가3456");

        TripStartRequest request2 = new TripStartRequest();
        request2.setVehicleId("VEHICLE_002");
        request2.setPlateNo("34나5678");

        // when
        carTrackingIntegrationService.notifyTripStarted("VEHICLE_001", "TRIP_STARTED", request1);
        carTrackingIntegrationService.notifyTripStarted("VEHICLE_002", "TRIP_STARTED", request2);

        // then
        verify(restTemplate, times(2)).postForObject(
                eq("http://localhost:8082/api/v1/tracking/trips/start"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("null 데이터 처리")
    void nullDataHandling() {
        // given
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn("SUCCESS");

        // when & then - null 데이터가 전달되어도 예외가 발생하지 않아야 함
        assertDoesNotThrow(() -> {
            carTrackingIntegrationService.notifyTripStarted("TEST_VEHICLE_001", "TRIP_STARTED", null);
        });

        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8082/api/v1/tracking/trips/start"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("빈 차량 ID 처리")
    void emptyVehicleIdHandling() {
        // given
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn("SUCCESS");

        // when & then - 빈 차량 ID가 전달되어도 예외가 발생하지 않아야 함
        assertDoesNotThrow(() -> {
            carTrackingIntegrationService.notifyTripStarted("", "TRIP_STARTED", tripStartRequest);
        });

        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8082/api/v1/tracking/trips/start"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("URL 설정 검증")
    void urlConfiguration() {
        // given
        String customUrl = "http://custom-server:9090";
        ReflectionTestUtils.setField(carTrackingIntegrationService, "carTrackingServiceUrl", customUrl);
        
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn("SUCCESS");

        // when
        carTrackingIntegrationService.notifyTripStarted("TEST_VEHICLE_001", "TRIP_STARTED", tripStartRequest);

        // then
        verify(restTemplate, times(1)).postForObject(
                eq(customUrl + "/api/v1/tracking/trips/start"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }
}
