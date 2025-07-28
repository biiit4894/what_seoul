package org.example.what_seoul.controller.citydata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.citydata.population.Population;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResGetPopulationDataDTO {
    @Schema(description = "실시간 인구 현황 데이터 ID", example = "1")
    private Long id;

    @Schema(description = """
            장소 혼잡도 지표 \s
            - 지표의 종류는 아래와 같습니다. (혼잡도 오름차순)
            - 여유, 보통, 약간 붐빔, 붐빔
            """,
            example = "보통")
    private String congestionLevel;

    @Schema(description = """
            장소 혼잡도 지표 관련 메시지
            - 혼잡도 지표의 종류에 따라 달라집니다.
            - 여유 : 사람이 몰려있을 가능성이 낮고 붐빔은 거의 느껴지지 않아요. 도보 이동이 자유로워요.
            - 보통 : 사람이 몰려있을 수 있지만 크게 붐비지는 않아요. 도보 이동에 큰 제약이 없어요.
            - 약간 붐빔 : 사람들이 몰려있을 가능성이 크고 붐빈다고 느낄 수 있어요. 인구밀도가 높은 구간에서는 도보 이동시 부딪힘이 발생할 수 있어요.
            - 붐빔 : 사람들이 몰려있을 가능성이 매우 크고 많이 붐빈다고 느낄 수 있어요. 인구밀도가 높은 구간에서는 도보 이동시 부딪힘이 발생할 수 있어요.
            """,
            example = "사람이 몰려있을 수 있지만 크게 붐비지는 않아요. 도보 이동에 큰 제약이 없어요.")
    private String congestionMessage;

    @Schema(description = "실시간 인구 지표 최소값 (단위: 명)", example = "24000")
    private String populationMin;

    @Schema(description = "실시간 인구 지표 최대값 (단위: 명)", example = "22000")
    private String populationMax;

    @Schema(description = "실시간 인구 데이터 업데이트 시간", example = "2025-07-23 18:30")
    private String populationUpdateTime;

    @Schema(description = "관련 장소 ID", example = "1")
    private Long areaId;

    @Schema(description = "예측 인구 현황 데이터 목록"
    )
    private List<PopulationForecastDTO> forecasts;

    public ResGetPopulationDataDTO(Population population) {
        this.id = population.getId();
        this.congestionLevel = population.getCongestionLevel();
        this.congestionMessage = population.getCongestionMessage();
        this.populationMin = population.getPopulationMin();
        this.populationMax = population.getPopulationMax();
        this.populationUpdateTime = population.getPopulationUpdateTime();
        this.areaId = population.getArea().getId();
        this.forecasts = population.getForecasts().stream()
                .map(PopulationForecastDTO::from)
                .collect(Collectors.toList());
    }

    public static ResGetPopulationDataDTO from(Population population) {
        return new ResGetPopulationDataDTO(population);
    }
}
