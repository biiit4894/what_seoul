package org.example.what_seoul.repository.cityData.population;

import org.example.what_seoul.domain.citydata.population.PopulationForecast;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopulationForecastRepository extends JpaRepository<PopulationForecast, Long> {
}
