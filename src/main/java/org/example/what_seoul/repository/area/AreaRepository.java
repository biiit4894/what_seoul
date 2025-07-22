package org.example.what_seoul.repository.area;

import org.example.what_seoul.domain.citydata.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area, Long>, AreaQueryRepository {
    boolean existsByAreaNameAndDeletedAtIsNull(String areaName);

    boolean existsByPolygonWktAndDeletedAtIsNull(String polygonWkt);

    Optional<Area> findByAreaCodeAndDeletedAtIsNull(String areaCode);

    Optional<Area> findByAreaNameAndDeletedAtIsNull(String areaName);

    List<Area> findByAreaNameContainingAndDeletedAtIsNull(String keyword);

    List<Area> findByDeletedAtIsNull();
}
