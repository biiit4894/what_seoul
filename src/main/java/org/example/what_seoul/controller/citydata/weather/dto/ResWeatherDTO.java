package org.example.what_seoul.controller.citydata.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.citydata.weather.Weather;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResWeatherDTO {
    private Long id;
    private String temperature;
    private String maxTemperature;
    private String minTemperature;
    private String pm25Index;
    private String pm25;
    private String pm10Index;
    private String pm10;
    private String pcpMsg;
    private String weatherUpdateTime;
    private Long areaId;

    public ResWeatherDTO(Weather weather) {
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

    public static ResWeatherDTO from(Weather weather) {
        return new ResWeatherDTO(weather);
    }
}
