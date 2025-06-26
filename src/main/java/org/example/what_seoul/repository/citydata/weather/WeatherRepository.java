package org.example.what_seoul.repository.citydata.weather;

import org.example.what_seoul.domain.citydata.weather.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeatherRepository extends JpaRepository<Weather, Long> {
    Optional<Weather> findByAreaId(Long areaId);
}
