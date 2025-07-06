package org.example.what_seoul.repository.citydata.weather;

import org.example.what_seoul.domain.citydata.weather.PcpMsgHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PcpMsgHistoryRepository extends JpaRepository<PcpMsgHistory, Long> {
    boolean existsByPcpMsg(String pcpMsg);
}
