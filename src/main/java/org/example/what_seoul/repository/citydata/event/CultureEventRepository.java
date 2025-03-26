package org.example.what_seoul.repository.citydata.event;

import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CultureEventRepository extends JpaRepository<CultureEvent, Long> {
    Optional<List<CultureEvent>> findAllByAreaId(Long areaId);
}
