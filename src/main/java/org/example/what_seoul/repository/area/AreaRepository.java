package org.example.what_seoul.repository.area;

import org.example.what_seoul.domain.citydata.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area, Long> {
    Optional<List<Area>> findByAreaNameContaining(String keyword);
}
