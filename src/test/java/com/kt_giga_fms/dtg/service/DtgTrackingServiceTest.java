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
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DtgTrackingService 단위 테스트")
class DtgTrackingServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private CarTrackingIntegrationService carTrackingIntegrationService;

    @Mock
    private CsvDataService csvDataService;

    @InjectMocks
    private DtgTrackingService dtgTrackingService;

    private TripStartRequest tripStartRequest;
    private TripEndRequest tripEndRequest;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    @DisplayName("운행 시작 - 정상 케이스")
    void startTrip_Success() {
        // given
        doNothing().when(carTrackingIntegrationService).notifyTripStarted(anyString(), anyString(), any(TripStartRequest.class));

        // when
        String tripId = dtgTrackingService.startTrip(tripStartRequest);

        // then
        assertNotNull(tripId);
        assertFalse(tripId.isEmpty());
        
        // 활성 운행에 추가되었는지 확인
        Map<String, TripSession> activeTrips = dtgTrackingService.getActiveTrips();
        assertTrue(activeTrips.containsKey("TEST_VEHICLE_001"));
        
        TripSession session = activeTrips.get("TEST_VEHICLE_001");
        assertEquals("TEST_VEHICLE_001", session.getVehicleId());
        assertEquals("12가3456", session.getPlateNo());
        assertEquals("DRIVER_001", session.getDriverId());
        assertEquals(37.5665, session.getStartLatitude());
        assertEquals(126.9780, session.getStartLongitude());
        assertEquals("서울시청", session.getDestination());
        assertEquals("업무", session.getPurpose());
        assertTrue(session.isActive());

        // 외부 서비스 호출 확인
        verify(carTrackingIntegrationService, times(1))
                .notifyTripStarted("TEST_VEHICLE_001", "TRIP_STARTED", tripStartRequest);
    }

    @Test
    @DisplayName("운행 종료 - 정상 케이스")
    void endTrip_Success() {
        // given
        doNothing().when(carTrackingIntegrationService).notifyTripStarted(anyString(), anyString(), any(TripStartRequest.class));
        doNothing().when(carTrackingIntegrationService).notifyTripEnded(anyString(), anyString(), any(TripEndRequest.class));

        // 먼저 운행 시작
        dtgTrackingService.startTrip(tripStartRequest);

        // when
        dtgTrackingService.endTrip(tripEndRequest);

        // then
        // 활성 운행에서 제거되었는지 확인
        Map<String, TripSession> activeTrips = dtgTrackingService.getActiveTrips();
        assertFalse(activeTrips.containsKey("TEST_VEHICLE_001"));

        // 외부 서비스 호출 확인
        verify(carTrackingIntegrationService, times(1))
                .notifyTripEnded("TEST_VEHICLE_001", "TRIP_ENDED", tripEndRequest);
    }

    @Test
    @DisplayName("운행 종료 - 활성 운행이 없는 경우")
    void endTrip_NoActiveTrip() {
        // given - 활성 운행이 없는 상태

        // when
        dtgTrackingService.endTrip(tripEndRequest);

        // then
        // 외부 서비스 호출되지 않음
        verify(carTrackingIntegrationService, never())
                .notifyTripEnded(anyString(), anyString(), any(TripEndRequest.class));
    }

    @Test
    @DisplayName("특정 차량의 운행 세션 조회 - 존재하는 경우")
    void getTripSession_Exists() {
        // given
        doNothing().when(carTrackingIntegrationService).notifyTripStarted(anyString(), anyString(), any(TripStartRequest.class));
        dtgTrackingService.startTrip(tripStartRequest);

        // when
        TripSession session = dtgTrackingService.getTripSession("TEST_VEHICLE_001");

        // then
        assertNotNull(session);
        assertEquals("TEST_VEHICLE_001", session.getVehicleId());
        assertEquals("12가3456", session.getPlateNo());
        assertTrue(session.isActive());
    }

    @Test
    @DisplayName("특정 차량의 운행 세션 조회 - 존재하지 않는 경우")
    void getTripSession_NotExists() {
        // when
        TripSession session = dtgTrackingService.getTripSession("NON_EXISTENT_VEHICLE");

        // then
        assertNull(session);
    }

    @Test
    @DisplayName("활성 운행 목록 조회")
    void getActiveTrips() {
        // given
        doNothing().when(carTrackingIntegrationService).notifyTripStarted(anyString(), anyString(), any(TripStartRequest.class));
        dtgTrackingService.startTrip(tripStartRequest);

        // when
        Map<String, TripSession> activeTrips = dtgTrackingService.getActiveTrips();

        // then
        assertNotNull(activeTrips);
        assertEquals(1, activeTrips.size());
        assertTrue(activeTrips.containsKey("TEST_VEHICLE_001"));
    }

    @Test
    @DisplayName("실시간 추적 데이터 전송 - CSV 데이터 사용")
    void sendTrackingData_WithCsvData() {
        // given
        doNothing().when(carTrackingIntegrationService).notifyTripStarted(anyString(), anyString(), any(TripStartRequest.class));
        when(csvDataService.isEnabled()).thenReturn(true);
        
        TrackingData mockTrackingData = new TrackingData();
        mockTrackingData.setVehicleId("TEST_VEHICLE_001");
        mockTrackingData.setLatitude(37.5665);
        mockTrackingData.setLongitude(126.9780);
        mockTrackingData.setSpeed(50.0);
        
        when(csvDataService.getNextTrackingDataAsTrackingData(eq("TEST_VEHICLE_001"), eq("12가3456"), anyString()))
                .thenReturn(mockTrackingData);
        doNothing().when(carTrackingIntegrationService).sendTrackingData(any(TrackingData.class));

        dtgTrackingService.startTrip(tripStartRequest);

        // when
        dtgTrackingService.sendTrackingData();

        // then
        verify(csvDataService, times(1)).isEnabled();
        verify(csvDataService, times(1))
                .getNextTrackingDataAsTrackingData(eq("TEST_VEHICLE_001"), eq("12가3456"), anyString());
        verify(carTrackingIntegrationService, times(1)).sendTrackingData(any(TrackingData.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(TrackingData.class));
    }

    @Test
    @DisplayName("실시간 추적 데이터 전송 - CSV 데이터 없음 (랜덤 데이터 사용)")
    void sendTrackingData_WithoutCsvData() {
        // given
        doNothing().when(carTrackingIntegrationService).notifyTripStarted(anyString(), anyString(), any(TripStartRequest.class));
        when(csvDataService.isEnabled()).thenReturn(false);
        doNothing().when(carTrackingIntegrationService).sendTrackingData(any(TrackingData.class));

        dtgTrackingService.startTrip(tripStartRequest);

        // when
        dtgTrackingService.sendTrackingData();

        // then
        verify(csvDataService, times(1)).isEnabled();
        verify(csvDataService, never())
                .getNextTrackingDataAsTrackingData(anyString(), anyString(), anyString());
        verify(carTrackingIntegrationService, times(1)).sendTrackingData(any(TrackingData.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(TrackingData.class));
    }

    @Test
    @DisplayName("실시간 추적 데이터 전송 - CSV 데이터 없음 (랜덤 데이터 사용)")
    void sendTrackingData_CsvDataNotFound() {
        // given
        doNothing().when(carTrackingIntegrationService).notifyTripStarted(anyString(), anyString(), any(TripStartRequest.class));
        when(csvDataService.isEnabled()).thenReturn(true);
        when(csvDataService.getNextTrackingDataAsTrackingData(eq("TEST_VEHICLE_001"), eq("12가3456"), anyString()))
                .thenReturn(null); // CSV 데이터 없음
        doNothing().when(carTrackingIntegrationService).sendTrackingData(any(TrackingData.class));

        dtgTrackingService.startTrip(tripStartRequest);

        // when
        dtgTrackingService.sendTrackingData();

        // then
        // 랜덤 데이터를 사용하므로 운행은 계속됨
        Map<String, TripSession> activeTrips = dtgTrackingService.getActiveTrips();
        assertTrue(activeTrips.containsKey("TEST_VEHICLE_001"));

        verify(csvDataService, times(1)).isEnabled();
        verify(csvDataService, times(1))
                .getNextTrackingDataAsTrackingData(eq("TEST_VEHICLE_001"), eq("12가3456"), anyString());
        verify(carTrackingIntegrationService, times(1)).sendTrackingData(any(TrackingData.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(TrackingData.class));
    }

    @Test
    @DisplayName("여러 차량 동시 운행 테스트")
    void multipleVehiclesSimultaneousTrips() {
        // given
        doNothing().when(carTrackingIntegrationService).notifyTripStarted(anyString(), anyString(), any(TripStartRequest.class));
        doNothing().when(carTrackingIntegrationService).notifyTripEnded(anyString(), anyString(), any(TripEndRequest.class));

        TripStartRequest request2 = new TripStartRequest();
        request2.setVehicleId("TEST_VEHICLE_002");
        request2.setPlateNo("34나5678");
        request2.setDriverId("DRIVER_002");
        request2.setStartLatitude(37.5665);
        request2.setStartLongitude(126.9780);
        request2.setDestination("강남역");
        request2.setPurpose("개인");

        // when
        String tripId1 = dtgTrackingService.startTrip(tripStartRequest);
        String tripId2 = dtgTrackingService.startTrip(request2);

        // then
        assertNotNull(tripId1);
        assertNotNull(tripId2);
        assertNotEquals(tripId1, tripId2);

        Map<String, TripSession> activeTrips = dtgTrackingService.getActiveTrips();
        assertEquals(2, activeTrips.size());
        assertTrue(activeTrips.containsKey("TEST_VEHICLE_001"));
        assertTrue(activeTrips.containsKey("TEST_VEHICLE_002"));

        // 첫 번째 차량만 종료
        dtgTrackingService.endTrip(tripEndRequest);
        
        activeTrips = dtgTrackingService.getActiveTrips();
        assertEquals(1, activeTrips.size());
        assertFalse(activeTrips.containsKey("TEST_VEHICLE_001"));
        assertTrue(activeTrips.containsKey("TEST_VEHICLE_002"));
    }
}
