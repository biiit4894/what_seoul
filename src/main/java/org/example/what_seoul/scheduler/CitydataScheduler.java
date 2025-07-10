package org.example.what_seoul.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.CityData;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.citydata.population.Population;
import org.example.what_seoul.domain.citydata.population.PopulationForecast;
import org.example.what_seoul.domain.citydata.weather.Weather;
import org.example.what_seoul.exception.CitydataSchedulerException;
import org.example.what_seoul.repository.area.AreaRepository;
import org.example.what_seoul.service.citydata.CitydataParser;
import org.example.what_seoul.service.citydata.CitydataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CitydataScheduler {
    private final CitydataService citydataService;
    private final CitydataParser citydataParser;
    private final AreaRepository areaRepository;

    @Value("${seoul.open.api.url}")
    private String url;


    /**
     * 인구 현황(+인구 예측값) 데이터, 날씨 현황 데이터, 문화행사 데이터 를 갱신한다.
     * - 5분 간격으로 배치 작업 수행
     * - 단, 문화 행사 데이터는 매일 00시, 06시, 12시, 18시에 갱신하도록 한다.
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void call() {
        LocalDateTime beforeTime = LocalDateTime.now(); // 작업 수행 시작 시간
        int hour = beforeTime.getHour();

        boolean isUpdateCultureEventHour = (hour % 6) == 0; // 문화행사 데이터를 저장하는 시간인지 여부

        // 서울시내 핫스팟 장소 116곳 조회
        List<Area> areas = areaRepository.findAll();

        List<CompletableFuture<CityData>> allFutures = areas.stream()
                .map(area -> fetchCityData(area, isUpdateCultureEventHour))
                .toList();

        List<Population> populationList = allFutures.stream()
                .map(CompletableFuture::join)
                .map(CityData::getPopulation)
                .collect(Collectors.toList());

        List<PopulationForecast> populationForecastList = allFutures.stream()
                .map(CompletableFuture::join)
                .flatMap(cityData -> cityData.getPopulationForecast().stream())
                .collect(Collectors.toList());

        List<Weather> weatherList = allFutures.stream()
                .map(CompletableFuture::join)
                .map(CityData::getWeather)
                .collect(Collectors.toList());


        // 기존 데이터 삭제 후 새 데이터 저장
        citydataService.updatePopulationAndWeatherData(populationList, populationForecastList, weatherList);

        if (isUpdateCultureEventHour) { // 매일 00시, 06시, 12시, 18시에만 진행
            List<CultureEvent> fetchedEvents = allFutures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(cityData -> cityData.getCultureEvent().stream())
                    .toList();
            citydataService.updateOrInsertCultureEventData(fetchedEvents);
        }

        LocalDateTime afterTime = LocalDateTime.now();
        log.info("호출 시작 시간 = {}", beforeTime);
        log.info("호출 종료 시간 = {}", afterTime);

        long totalTime = java.time.Duration.between(beforeTime, afterTime).getSeconds();
        log.info("소요 시간 = {}초", totalTime);

    }


    /**
     * 특정 지역에 대해 비동기로 도시 데이터를 조회하는 메소드.
     * 내부적으로 fetchCityDataSync를 별도 스레드에서 실행한다.
     * @param area                          도시데이터를 조회할 Area 정보
     * @param isUpdateCultureEventHour      문화행사 데이터 갱신 여부 플래그
     * @return CompletableFuture<CityData>  비동기 처리 결과를 포함하는 Future 객체
     */
    CompletableFuture<CityData> fetchCityData(Area area, boolean isUpdateCultureEventHour) {
        return CompletableFuture.supplyAsync(() -> fetchCityDataSync(area, isUpdateCultureEventHour));

    }

    /**
     * 특정 장소에 대해 동기적으로 도시 데이터를 조회하는 메소드.
     * 공공데이터 API에서 XML Document를 가져오고, 이를 Population, PopulationForecast, Weather, CultureEvent로 파싷애 반환한다.
     *
     * @param area                        조회할 장소 정보
     * @param isUpdateCultureEventHour    문화행사 데이터 갱신 여부 플래그
     * @return CityData                   조회된 도시 데이터 객체
     * @throws CitydataSchedulerException XML 파싱이나 네트워크 요청 실패 시 발생
     */
    public CityData fetchCityDataSync(Area area, boolean isUpdateCultureEventHour) {
        try {
            Document document = getDocument(area);

            Population population = citydataParser.parsePopulationData(document, area);
            List<PopulationForecast> populationForecast = citydataParser.parsePopulationForecastData(document, population, area);
            Weather weather = citydataParser.parseWeatherData(document, area);

            List<CultureEvent> cultureEvent = isUpdateCultureEventHour ? citydataParser.parseCultureEventData(document, area) : Collections.emptyList();

            return new CityData(population, populationForecast, weather, cultureEvent);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("failed to fetch city data for area {}: {}", area.getAreaName(), e.getMessage());
            throw new CitydataSchedulerException("도시 데이터 fetch에 실패했습니다.");
        }
    }

    /**
     * 특정 장소의 공공데이터 API URL로부터 XML Document를 가져오는 메소드.
     * 지역 이름에 포함된 '&' 문자를 안전하게 치환하고 URL 인코딩 후 요청한다.
     *
     * @param area 조회할 장소 정보
     * @return Document 요청한 XML 문서 객체
     * @throws ParserConfigurationException 문서 빌더 구성 오류 발생 시
     * @throws SAXException XML 파싱 오류 발생 시
     * @throws IOException 네트워크 I/O 오류 발생 시
     */
    protected Document getDocument(Area area) throws ParserConfigurationException, SAXException, IOException {
        log.info("getDocument called with area: " + area);
        String sanitizedAreaName = area.getAreaName().replace("&", "&amp;");
        String encodedAreaName = URLEncoder.encode(sanitizedAreaName, StandardCharsets.UTF_8);
        log.info("sanitized area name: {}, id: {}", sanitizedAreaName, area.getId());
        log.info("encoded area name: {}", encodedAreaName);

        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(url + encodedAreaName);
        document.getDocumentElement().normalize();
        return document;
    }
}
