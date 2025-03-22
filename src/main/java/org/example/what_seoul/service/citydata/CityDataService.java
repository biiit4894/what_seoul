package org.example.what_seoul.service.citydata;

import org.example.what_seoul.repository.cityData.AreaRepository;
import org.example.what_seoul.repository.cityData.population.PopulationRepository;
import org.springframework.stereotype.Service;

@Service
public class CityDataService {
    private final AreaRepository areaRepository;
    private final PopulationRepository populationRepository;

    public CityDataService(AreaRepository areaRepository, PopulationRepository populationRepository) {
        this.areaRepository = areaRepository;
        this.populationRepository = populationRepository;
    }

    public void fetchAndSaveCityData() {

    }
}
