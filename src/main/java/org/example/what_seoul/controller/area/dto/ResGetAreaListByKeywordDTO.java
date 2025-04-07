package org.example.what_seoul.controller.area.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResGetAreaListByKeywordDTO {
    private List<AreaDTO> areaList;
}
