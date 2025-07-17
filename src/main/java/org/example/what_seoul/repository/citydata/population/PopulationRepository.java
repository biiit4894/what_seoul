package org.example.what_seoul.repository.citydata.population;

import org.example.what_seoul.domain.citydata.population.Population;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PopulationRepository extends JpaRepository<Population, Long> {
    Optional<Population> findByAreaId(Long areaId);

    Optional<Population> findTopByAreaIdOrderByCreatedAtDesc(Long areaId);
}
