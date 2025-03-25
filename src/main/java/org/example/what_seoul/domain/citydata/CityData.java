package org.example.what_seoul.domain.citydata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.what_seoul.domain.citydata.population.Population;

/*
도시데이터
 */
@RequiredArgsConstructor
@Getter
public class CityData {
    private final Population population;
}
