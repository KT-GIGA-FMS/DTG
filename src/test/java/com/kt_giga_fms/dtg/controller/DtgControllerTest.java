package com.kt_giga_fms.dtg.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kt_giga_fms.dtg.dto.ApiResponse;
import com.kt_giga_fms.dtg.dto.TripEndRequest;
import com.kt_giga_fms.dtg.dto.TripStartRequest;
import com.kt_giga_fms.dtg.service.DtgTrackingService;
import com.kt_giga_fms.dtg.service.TripSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DtgController.class)
@DisplayName("DtgController API 테스트")
class DtgControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DtgTrackingService dtgTrackingService;

    @Autowired
    private ObjectMapper objectMapper;

    private TripStartRequest tripStartRequest;
    private TripEndRequest tripEndRequest;
    private TripSession tripSession;

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

        // 테스트용 TripSession 설정
        tripSession = new TripSession(
                "TRIP_001",
                "TEST_VEHICLE_001",
                "12가3456",
                "DRIVER_001",
                37.5665,
                126.9780,
                "서울시청",
                "업무"
        );
        tripSession.setStartTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("운행 시작 API - 성공")
    void startTrip_Success() throws Exception {
        // given
        String tripId = "TRIP_001";
        when(dtgTrackingService.startTrip(any(TripStartRequest.class))).thenReturn(tripId);

        // when & then
        mockMvc.perform(post("/api/v1/dtg/trips/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripStartRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("운행이 시작되었습니다."))
                .andExpect(jsonPath("$.data").value(tripId));

        verify(dtgTrackingService, times(1)).startTrip(any(TripStartRequest.class));
    }

    @Test
    @DisplayName("운행 시작 API - 실패 (서비스 예외)")
    void startTrip_Failure() throws Exception {
        // given
        when(dtgTrackingService.startTrip(any(TripStartRequest.class)))
                .thenThrow(new RuntimeException("운행 시작 실패"));

        // when & then
        mockMvc.perform(post("/api/v1/dtg/trips/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripStartRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("운행 시작에 실패했습니다: 운행 시작 실패"));

        verify(dtgTrackingService, times(1)).startTrip(any(TripStartRequest.class));
    }

    @Test
    @DisplayName("운행 시작 API - 유효성 검사 실패")
    void startTrip_ValidationFailure() throws Exception {
        // given - 필수 필드가 누락된 요청
        TripStartRequest invalidRequest = new TripStartRequest();
        // vehicleId가 null

        // when & then
        mockMvc.perform(post("/api/v1/dtg/trips/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(dtgTrackingService, never()).startTrip(any(TripStartRequest.class));
    }

    @Test
    @DisplayName("운행 종료 API - 성공")
    void endTrip_Success() throws Exception {
        // given
        doNothing().when(dtgTrackingService).endTrip(any(TripEndRequest.class));

        // when & then
        mockMvc.perform(post("/api/v1/dtg/trips/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripEndRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("운행이 종료되었습니다."));

        verify(dtgTrackingService, times(1)).endTrip(any(TripEndRequest.class));
    }

    @Test
    @DisplayName("운행 종료 API - 실패 (서비스 예외)")
    void endTrip_Failure() throws Exception {
        // given
        doThrow(new RuntimeException("운행 종료 실패"))
                .when(dtgTrackingService).endTrip(any(TripEndRequest.class));

        // when & then
        mockMvc.perform(post("/api/v1/dtg/trips/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripEndRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("운행 종료에 실패했습니다: 운행 종료 실패"));

        verify(dtgTrackingService, times(1)).endTrip(any(TripEndRequest.class));
    }

    @Test
    @DisplayName("활성 운행 목록 조회 API - 성공")
    void getActiveTrips_Success() throws Exception {
        // given
        Map<String, TripSession> activeTrips = new HashMap<>();
        activeTrips.put("TEST_VEHICLE_001", tripSession);
        when(dtgTrackingService.getActiveTrips()).thenReturn(activeTrips);

        // when & then
        mockMvc.perform(get("/api/v1/dtg/trips/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.TEST_VEHICLE_001").exists())
                .andExpect(jsonPath("$.data.TEST_VEHICLE_001.vehicleId").value("TEST_VEHICLE_001"));

        verify(dtgTrackingService, times(1)).getActiveTrips();
    }

    @Test
    @DisplayName("활성 운행 목록 조회 API - 빈 목록")
    void getActiveTrips_Empty() throws Exception {
        // given
        Map<String, TripSession> emptyTrips = new HashMap<>();
        when(dtgTrackingService.getActiveTrips()).thenReturn(emptyTrips);

        // when & then
        mockMvc.perform(get("/api/v1/dtg/trips/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(dtgTrackingService, times(1)).getActiveTrips();
    }

    @Test
    @DisplayName("활성 운행 목록 조회 API - 실패 (서비스 예외)")
    void getActiveTrips_Failure() throws Exception {
        // given
        when(dtgTrackingService.getActiveTrips())
                .thenThrow(new RuntimeException("조회 실패"));

        // when & then
        mockMvc.perform(get("/api/v1/dtg/trips/active"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("활성 운행 목록 조회에 실패했습니다: 조회 실패"));

        verify(dtgTrackingService, times(1)).getActiveTrips();
    }

    @Test
    @DisplayName("특정 차량 운행 상태 조회 API - 성공")
    void getTripStatus_Success() throws Exception {
        // given
        when(dtgTrackingService.getTripSession("TEST_VEHICLE_001")).thenReturn(tripSession);

        // when & then
        mockMvc.perform(get("/api/v1/dtg/trips/{vehicleId}", "TEST_VEHICLE_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.vehicleId").value("TEST_VEHICLE_001"))
                .andExpect(jsonPath("$.data.plateNo").value("12가3456"));

        verify(dtgTrackingService, times(1)).getTripSession("TEST_VEHICLE_001");
    }

    @Test
    @DisplayName("특정 차량 운행 상태 조회 API - 운행 없음")
    void getTripStatus_NotFound() throws Exception {
        // given
        when(dtgTrackingService.getTripSession("NON_EXISTENT_VEHICLE")).thenReturn(null);

        // when & then
        mockMvc.perform(get("/api/v1/dtg/trips/{vehicleId}", "NON_EXISTENT_VEHICLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("해당 차량의 활성 운행이 없습니다."))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(dtgTrackingService, times(1)).getTripSession("NON_EXISTENT_VEHICLE");
    }

    @Test
    @DisplayName("특정 차량 운행 상태 조회 API - 실패 (서비스 예외)")
    void getTripStatus_Failure() throws Exception {
        // given
        when(dtgTrackingService.getTripSession("TEST_VEHICLE_001"))
                .thenThrow(new RuntimeException("조회 실패"));

        // when & then
        mockMvc.perform(get("/api/v1/dtg/trips/{vehicleId}", "TEST_VEHICLE_001"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("차량 운행 상태 조회에 실패했습니다: 조회 실패"));

        verify(dtgTrackingService, times(1)).getTripSession("TEST_VEHICLE_001");
    }

    @Test
    @DisplayName("헬스 체크 API - 성공")
    void getHealth_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/dtg/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("DTG 서비스가 정상적으로 실행 중입니다."));
    }

    @Test
    @DisplayName("잘못된 HTTP 메서드 요청")
    void invalidHttpMethod() throws Exception {
        // when & then
        mockMvc.perform(put("/api/v1/dtg/trips/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripStartRequest)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("잘못된 URL 요청")
    void invalidUrl() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/dtg/invalid"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Content-Type이 JSON이 아닌 요청")
    void invalidContentType() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/dtg/trips/start")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("빈 요청 본문")
    void emptyRequestBody() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/dtg/trips/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 JSON 형식")
    void invalidJsonFormat() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/dtg/trips/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }
}
