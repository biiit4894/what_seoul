package org.example.what_seoul.service.citydata;

import lombok.RequiredArgsConstructor;
import org.example.what_seoul.domain.citydata.weather.PcpMsgHistory;
import org.example.what_seoul.repository.citydata.weather.PcpMsgHistoryRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PcpMsgHistoryService {
    private final PcpMsgHistoryRepository pcpMsgHistoryRepository;

    public void saveIfNotExists(String pcpMsg) {
        if (!pcpMsgHistoryRepository.existsByPcpMsg(pcpMsg)) {
            pcpMsgHistoryRepository.save(new PcpMsgHistory(pcpMsg));
        }
    }

}
