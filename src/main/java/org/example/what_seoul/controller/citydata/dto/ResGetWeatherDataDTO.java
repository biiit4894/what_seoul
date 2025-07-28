package org.example.what_seoul.controller.citydata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.citydata.weather.Weather;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResGetWeatherDataDTO {
    @Schema(description = "실시간 날씨 현황 데이터 ID", example = "1095451")
    private Long id;

    @Schema(description = "기온", example = "34.0")
    private String temperature;

    @Schema(description = "최고 기온", example = "37.0")
    private String maxTemperature;

    @Schema(description = "최저 기온", example = "28.0")
    private String minTemperature;

    @Schema(description = "초미세먼지 지표", example = "좋음")
    private String pm25Index;

    @Schema(description = "초미세먼지 농도", example = "6")
    private String pm25;

    @Schema(description = "미세먼지 지표", example = "좋음")
    private String pm10Index;

    @Schema(description = "미세먼지 농도", example = "11")
    private String pm10;

    @Schema(description = """
            강수 관련 메시지\s
            - 메시지 유형은 아래와 같습니다. (강수량 오름차순)\s
              - 비 또는 눈 소식이 없어요.\s
              - 약한 비가 내리고 있어요.외출 시 우산을 챙기세요.\s
              - 비가 내리고 있어요.외출 시 우산을 챙기세요.\s
              - 강한 비가 내리고 있어요.외출 시 우산을 챙기세요.보행 및 교통 안전에 유의해주세요.\s
              - 매우 강한 비가 내리고 있어요.외출 시 우산을 챙기세요.보행 및 교통 안전에 각별히 유의해주세요.
            """,
            example = "비 또는 눈 소식이 없어요."
    )
    private String pcpMsg;

    @Schema(description = "날씨 데이터 업데이트 시간", example = "2025-07-27 11:51")
    private String weatherUpdateTime;

    @Schema(description = "관련 장소 ID", example = "1")
    private Long areaId;

    public ResGetWeatherDataDTO(Weather weather) {
        this.id = weather.getId();
        this.temperature = weather.getTemperature();
        this.maxTemperature = weather.getMaxTemperature();
        this.minTemperature = weather.getMinTemperature();
        this.pm25Index = weather.getPm25Index();
        this.pm25 = weather.getPm25();
        this.pm10Index = weather.getPm10Index();
        this.pm10 = weather.getPm10();
        this.pcpMsg = weather.getPcpMsg();
        this.weatherUpdateTime = weather.getWeatherUpdateTime();
        this.areaId = weather.getArea().getId();
    }

    public static ResGetWeatherDataDTO from(Weather weather) {
        return new ResGetWeatherDataDTO(weather);
    }
}
