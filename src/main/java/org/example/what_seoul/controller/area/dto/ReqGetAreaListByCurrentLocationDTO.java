package org.example.what_seoul.controller.area.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReqGetAreaListByCurrentLocationDTO {
    private double latitude;
    private double longitude;
}
