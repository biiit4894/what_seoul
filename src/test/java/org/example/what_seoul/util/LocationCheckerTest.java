package org.example.what_seoul.util;

import org.example.what_seoul.controller.area.dto.AreaDTO;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.repository.area.AreaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocationCheckerTest {

    @Mock
    private AreaRepository areaRepository;

    @InjectMocks
    private LocationChecker locationChecker;

    @Test
    @DisplayName("[성공] LocationChcekr 테스트")
    void findLocations() {
        // Given
        String polygonWkt = "POLYGON((127.0 37.0, 127.1 37.0, 127.1 37.1, 127.0 37.1, 127.0 37.0))";
        Area area = new Area(null, null, "test", polygonWkt);

        when(areaRepository.findByDeletedAtIsNull()).thenReturn(List.of(area));

        // When
        List<AreaDTO> result = locationChecker.findLocations(37.05, 127.05);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAreaName()).isEqualTo("test");
    }

    @Test
    @DisplayName("[대체 경로] 포함하는 지역이 없을 때, 가장 가까운 3개 지역 반환")
    void findLocations_returnsNearestThree_whenNoContainingPolygon() {
        // Given
        String polygon1 = "POLYGON((127.0 37.0, 127.1 37.0, 127.1 37.1, 127.0 37.1, 127.0 37.0))";
        String polygon2 = "POLYGON((128.0 38.0, 128.1 38.0, 128.1 38.1, 128.0 38.1, 128.0 38.0))";
        String polygon3 = "POLYGON((129.0 39.0, 129.1 39.0, 129.1 39.1, 129.0 39.1, 129.0 39.0))";
        String polygon4 = "POLYGON((130.0 40.0, 130.1 40.0, 130.1 40.1, 130.0 40.1, 130.0 40.0))";

        Area area2 = new Area(null, null, "area2", polygon2);
        Area area1 = new Area(null, null, "area1", polygon1);
        Area area3 = new Area(null, null, "area3", polygon3);
        Area area4 = new Area(null, null, "area4", polygon4);

        when(areaRepository.findByDeletedAtIsNull()).thenReturn(List.of(area1, area2, area3, area4));

        // 사용자 위치가 어떤 폴리곤에도 포함되지 않음
        double userLat = 36.0;
        double userLon = 126.0;

        // When
        List<AreaDTO> result = locationChecker.findLocations(userLat, userLon);

        // Then
        assertThat(result).hasSize(3); // 가장 가까운 3개만 반환
        List<String> areaNames = result.stream().map(AreaDTO::getAreaName).toList();
        assertThat(areaNames).containsExactlyInAnyOrder("area1", "area2", "area3");
    }

    @Test
    @DisplayName("[예외] 잘못된 WKT 문자열로 인한 ParseException")
    void findLocations_throwsRuntimeException_whenInvalidWKT() {
        // Given
        String invalidWkt = "INVALID_WKT";
        Area area = new Area(null, null, "invalidArea", invalidWkt);

        when(areaRepository.findByDeletedAtIsNull()).thenReturn(List.of(area));

        // When & Then
        assertThatThrownBy(() -> locationChecker.findLocations(37.0, 127.0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid polygon data for area");
    }



}
