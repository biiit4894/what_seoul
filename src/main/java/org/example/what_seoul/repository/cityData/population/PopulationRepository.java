package org.example.what_seoul.repository.cityData.population;

import org.example.what_seoul.domain.cityData.population.Population;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopulationRepository extends JpaRepository<Population, Long> {
}
