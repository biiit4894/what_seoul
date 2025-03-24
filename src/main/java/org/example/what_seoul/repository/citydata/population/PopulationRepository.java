package org.example.what_seoul.repository.citydata.population;

import org.example.what_seoul.domain.citydata.population.Population;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopulationRepository extends JpaRepository<Population, Long> {
}
