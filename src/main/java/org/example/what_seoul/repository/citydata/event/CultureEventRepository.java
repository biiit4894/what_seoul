package org.example.what_seoul.repository.citydata.event;

import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CultureEventRepository extends JpaRepository<CultureEvent, Long> {
}
