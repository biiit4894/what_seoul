package org.example.what_seoul.repository.area;

import org.example.what_seoul.controller.area.dto.AreaWithCongestionLevelDTO;
import org.example.what_seoul.domain.citydata.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area, Long> {
    Optional<List<Area>> findByAreaNameContaining(String keyword);

    @Query("""
        SELECT new org.example.what_seoul.controller.area.dto.AreaWithCongestionLevelDTO(
        p.id,
        a.id,
        a.areaName,
        a.polygonWkt,
        p.congestionLevel
    )
    FROM Population p
    JOIN p.area a
""")
    List<AreaWithCongestionLevelDTO> findAllAreasWithCongestionLevel();
}
