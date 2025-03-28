package org.example.what_seoul.controller.citydata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReqLocationDTO {
    private double latitude;
    private double longitude;
}
