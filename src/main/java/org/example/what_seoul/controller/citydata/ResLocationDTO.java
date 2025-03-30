package org.example.what_seoul.controller.citydata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResLocationDTO {
    private List<String> nearestPlaces;
}
