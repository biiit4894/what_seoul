package org.example.what_seoul.repository.area;

import org.example.what_seoul.controller.area.dto.AreaWithCongestionLevelDTO;
import org.example.what_seoul.controller.area.dto.AreaWithWeatherDTO;
import org.example.what_seoul.domain.citydata.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area, Long>, AreaQueryRepository {
    boolean existsByAreaNameAndDeletedAtIsNull(String areaName);

    boolean existsByPolygonWktAndDeletedAtIsNull(String polygonWkt);

    Optional<Area> findByAreaCodeAndDeletedAtIsNull(String areaCode);

    Optional<Area> findByAreaNameAndDeletedAtIsNull(String areaName);


    Optional<List<Area>> findByAreaNameContainingAndDeletedAtIsNull(String keyword);

    List<Area> findByDeletedAtIsNull();

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
    WHERE a.deletedAt IS NULL
""")
    List<AreaWithCongestionLevelDTO> findAllAreasWithCongestionLevel();


    @Query("""
        SELECT new org.example.what_seoul.controller.area.dto.AreaWithWeatherDTO(
        w.id,
        a.id,
        a.areaName,
        a.polygonWkt,
        w.temperature,
        w.pcpMsg
    )
    FROM Weather w
    JOIN w.area a
    WHERE a.deletedAt IS NULL
""")
    List<AreaWithWeatherDTO> findAllAreasWithWeather();
}
