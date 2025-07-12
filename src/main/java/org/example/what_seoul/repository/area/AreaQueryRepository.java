package org.example.what_seoul.repository.area;

import org.example.what_seoul.controller.admin.dto.ResGetAreaListDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface AreaQueryRepository {
    List<String> findAreaNamesByUserId(Long userId);

    Slice<ResGetAreaListDTO> findAreasSlice(String areaName, Pageable pageable);

}
