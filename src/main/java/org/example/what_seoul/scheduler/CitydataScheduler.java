package org.example.what_seoul.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.citydata.population.Population;
import org.example.what_seoul.domain.citydata.population.PopulationForecast;
import org.example.what_seoul.domain.citydata.weather.Weather;
import org.example.what_seoul.repository.area.AreaRepository;
import org.example.what_seoul.repository.citydata.event.CultureEventRepository;
import org.example.what_seoul.repository.citydata.population.PopulationForecastRepository;
import org.example.what_seoul.repository.citydata.population.PopulationRepository;
import org.example.what_seoul.repository.citydata.weather.WeatherRepository;
import org.example.what_seoul.service.citydata.PcpMsgHistoryService;
import org.example.what_seoul.util.XmlElementNames;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Component
@RequiredArgsConstructor
@Slf4j
public class CitydataScheduler {
    private final AreaRepository areaRepository;
    private final PopulationRepository populationRepository;
    private final PopulationForecastRepository populationForecastRepository;
    private final WeatherRepository weatherRepository;
    private final CultureEventRepository cultureEventRepository;

    private final PcpMsgHistoryService pcpMsgHistoryService;

    @Value("${seoul.open.api.url}")
    private String url;

    /**
     * 5분 간격으로 배치 작업 수행 -> 임시적으로 60분
     */
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void call() {
        LocalDateTime beforeTime = LocalDateTime.now();

        // 서울시내 핫스팟 장소 116곳 조회
        List<Area> areas = areaRepository.findAll();

        List<Population> populationList = new ArrayList<>();
        List<PopulationForecast> populationForecastList = new ArrayList<>();
        List<Weather> weatherList = new ArrayList<>();
        List<CultureEvent> cultureEventList = new ArrayList<>();

        for (Area area : areas) {
            // 각 장소에 대한 도시데이터 fetch
            Document document = fetchCityData(area);
            if (document == null) {
                log.warn("skipping area {} because document is null", area.getAreaName());
                continue;
            }

            // 실시간 인구 현황 데이터 파싱
            Population population = parsePopulationData(document, area);
            populationList.add(population);

            // 인구 예측값 데이터 파싱
            List<PopulationForecast> forecastList = parsePopulationForecastData(document, population);
            populationForecastList.addAll(forecastList);

            // 날씨 현황 데이터 파싱
            Weather weather = parseWeatherData(document, area);
            weatherList.add(weather);

            // 문화행사 현황 데이터 파싱
            List<CultureEvent> cultureEvent = parseCultureEventData(document, area);
            cultureEventList.addAll(cultureEvent);

        }

        // 기존 데이터 삭제 후 새 데이터 저장
        populationRepository.deleteAll();
        populationForecastRepository.deleteAll();
        weatherRepository.deleteAll();
        cultureEventRepository.deleteAll();

        populationRepository.saveAll(populationList);
        populationForecastRepository.saveAll(populationForecastList);
        weatherRepository.saveAll(weatherList);
        cultureEventRepository.saveAll(cultureEventList);

        LocalDateTime afterTime = LocalDateTime.now();
        log.info("호출 시작 시간 = " + beforeTime);
        log.info("호출 종료 시간 = " + afterTime);

        long totalTime = java.time.Duration.between(beforeTime, afterTime).getSeconds();
        log.info("소요 시간 = " + totalTime + " 초");

    }

    /**
     * 특정 장소의 도시 데이터를 공공데이터 API에서 가져와 XML Document로 반환
     *
     * @param area
     * @return
     */
    private Document fetchCityData(Area area) {
        try {
            String sanitizedAreaName = area.getAreaName().replace("&", "&amp;");
            String encodedAreaName = URLEncoder.encode(sanitizedAreaName, StandardCharsets.UTF_8);
            log.info("sanitized area name: " + sanitizedAreaName);
            log.info("encoded area name: " + encodedAreaName);

            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(url + encodedAreaName);
            document.getDocumentElement().normalize();
            return document;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("failed to fetch city data for area: {}", area.getAreaName());
            log.error("exception message: {}", e.getMessage());
            return null;
        }
    }

    /**
     * XML 문서에서 인구 데이터를 파싱하여 Population 객체 생성
     * @param document
     * @param area
     * @return
     */
    private Population parsePopulationData(Document document, Area area) {
        NodeList populationNodeList = document.getElementsByTagName(XmlElementNames.LIVE_PPLTN_STTS.getXmlElementName());

        String congestionLevel = "No Tag";
        String congestionMessage = "No Tag";
        String minPopulation = "No Tag";
        String maxPopulation = "No Tag";
        String populationUpdateTime = "No Tag";

        if (populationNodeList != null && populationNodeList.getLength() > 0) {
            Node populationParentNode = populationNodeList.item(1);
            if (populationParentNode == null) {
                log.warn("population parent node is null for area: {}", area.getAreaName());
            } else {
                NodeList childNodes = populationParentNode.getChildNodes();

                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node childNode = childNodes.item(i);
                    if (childNode.getNodeName().equals(XmlElementNames.AREA_CONGEST_LVL.getXmlElementName())) {
                        congestionLevel = childNode.getTextContent();
                    } else if (childNode.getNodeName().equals(XmlElementNames.AREA_CONGEST_MSG.getXmlElementName())) {
                        congestionMessage = childNode.getTextContent();
                    } else if (childNode.getNodeName().equals(XmlElementNames.AREA_PPLTN_MIN.getXmlElementName())) {
                        minPopulation = childNode.getTextContent();
                    } else if (childNode.getNodeName().equals(XmlElementNames.AREA_PPLTN_MAX.getXmlElementName())) {
                        maxPopulation = childNode.getTextContent();
                    } else if (childNode.getNodeName().equals(XmlElementNames.PPLTN_TIME.getXmlElementName())) {
                        populationUpdateTime = childNode.getTextContent();
                    }
                }
            }
        }
        return new Population(
                congestionLevel, congestionMessage, minPopulation, maxPopulation, populationUpdateTime, area
        );
    }

    /**
     * XML 문서에서 인구 예측 데이터를 파싱하여 PopulationForecast 객체 리스트 생성
     *
     * @param document
     * @param population
     * @return
     */
    private List<PopulationForecast> parsePopulationForecastData(Document document, Population population) {
        List<PopulationForecast> forecastList = new ArrayList<>();
        NodeList nodeList = document.getElementsByTagName(XmlElementNames.FCST_PPLTN.getXmlElementName());

        String forecastCongestionLevel = "No Tag";
        String forecastPpltnMin = "No Tag";
        String forecastPpltnMax = "No Tag";
        String forecasePplTime = "No Tag";

        if (nodeList == null || nodeList.getLength() == 0) {
            log.warn("No population forecast data found for area: {}", population.getArea().getAreaName());
        }
        for (int i = 1; i < nodeList.getLength(); i++) {
            Node fcstPpltnNode = nodeList.item(i);
            NodeList childNodes = fcstPpltnNode.getChildNodes();

            for (int j = 0; j < childNodes.getLength(); j++) {
                Node childNode = childNodes.item(j);
                if (childNode.getNodeName().equals(XmlElementNames.FCST_CONGEST_LVL.getXmlElementName())) {
                    forecastCongestionLevel = childNode.getTextContent();
                } else if (childNode.getNodeName().equals(XmlElementNames.FCST_PPLTN_MIN.getXmlElementName())) {
                    forecastPpltnMin = childNode.getTextContent();
                } else if (childNode.getNodeName().equals(XmlElementNames.FCST_PPLTN_MAX.getXmlElementName())) {
                    forecastPpltnMax = childNode.getTextContent();
                } else if (childNode.getNodeName().equals(XmlElementNames.FCST_TIME.getXmlElementName())) {
                    forecasePplTime = childNode.getTextContent();
                }
            }

            forecastList.add(new PopulationForecast(
                    forecastCongestionLevel,
                    forecastPpltnMin,
                    forecastPpltnMax,
                    forecasePplTime,
                    population));

        }
        return forecastList;
    }

    /**
     * XML 문서에서 날씨 데이터를 파싱하여 PopulationForecast 객체 리스트 생성
     *
     * @param document
     * @param area
     * @return
     */
    private Weather parseWeatherData(Document document, Area area) {
        // 날씨 데이터를 저장할 Map
        Map<XmlElementNames, String> weatherData = new HashMap<>();
        // 날씨 데이터를 담고 있는 XML 노드들 조회
        NodeList weatherNodeList = document.getElementsByTagName(XmlElementNames.WEATHER_STTS.getXmlElementName());
        // 인덱스 1의 노드를 가져오기 (최상위 노드의 하위 노드에 필요한 노드들이 위치함)
        Node weatherParentNode = weatherNodeList.item(1);
        if (weatherParentNode == null) {
            log.warn("weather parent node is null for area: {}", area.getAreaName());
        } else {
            // 자식 노드들에 대해 반복하며 날씨 정보를 Map에 저장
            NodeList childNodeList = weatherParentNode.getChildNodes();
            for (int i = 0; i < childNodeList.getLength(); i++) {
                Node childNode = childNodeList.item(i);

                // 각 자식 노드의 이름이 enum에 정의된 이름과 일치하면,
                // 그 노드를 Map의 key로, 노드의 텍스트 내용을 value로 저장
                for (XmlElementNames element : XmlElementNames.values()) {
                    if (childNode.getNodeName().equals(element.getXmlElementName())) {
                        weatherData.put(element, childNode.getTextContent());
                        break;
                    }
                }
            }
        }

        // PCP_MSG 값 추출
        String pcpMsg = weatherData.getOrDefault(XmlElementNames.PCP_MSG, "No Tag");

        // 히스토리 저장
        pcpMsgHistoryService.saveIfNotExists(pcpMsg);

        // Map에서 주어진 key(XmlElementNames)에 해당하는 값을 가져오고, 만약 해당 key가 없다면 "No Tag"를 기본값으로 반환
        return new Weather(
                weatherData.getOrDefault(XmlElementNames.TEMP, "No Tag"),
                weatherData.getOrDefault(XmlElementNames.MAX_TEMP, "No Tag"),
                weatherData.getOrDefault(XmlElementNames.MIN_TEMP, "No Tag"),
                weatherData.getOrDefault(XmlElementNames.PM25_INDEX, "No Tag"),
                weatherData.getOrDefault(XmlElementNames.PM25, "No Tag"),
                weatherData.getOrDefault(XmlElementNames.PM10_INDEX, "No Tag"),
                weatherData.getOrDefault(XmlElementNames.PM10, "No Tag"),
                weatherData.getOrDefault(XmlElementNames.PCP_MSG, "No Tag"),
                weatherData.getOrDefault(XmlElementNames.WEATHER_TIME, "No Tag"),
                area
        );
    }

    private List<CultureEvent> parseCultureEventData(Document document, Area area) {
        List<CultureEvent> cultureEventList = new ArrayList<>();
        NodeList cultureEventNodeList = document.getElementsByTagName(XmlElementNames.EVENT_STTS.getXmlElementName());
        log.info("area: {}, area id: {}, length: {}", area.getAreaName(), area.getId(), document.getElementsByTagName(XmlElementNames.EVENT_STTS.getXmlElementName()).getLength());
        String eventNm = "No Tag";
        String eventPeriod = "No Tag";
        String eventPlace = "No Tag";
        String eventX = "No Tag";
        String eventY = "No Tag";
        String thumbnail = "No Tag";
        String url = "No Tag";

        if (cultureEventNodeList != null && cultureEventNodeList.getLength() != 0) {

            for (int i = 1; i < cultureEventNodeList.getLength(); i++) {
                Node cultureEventNode = cultureEventNodeList.item(i);
                NodeList childNodes = cultureEventNode.getChildNodes();

                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node childNode = childNodes.item(j);
                    if (childNode.getNodeName().equals(XmlElementNames.EVENT_NM.getXmlElementName())) {
                        eventNm = childNode.getTextContent();
                    } else if (childNode.getNodeName().equals(XmlElementNames.EVENT_PERIOD.getXmlElementName())) {
                        eventPeriod = childNode.getTextContent();
                    } else if (childNode.getNodeName().equals(XmlElementNames.EVENT_PLACE.getXmlElementName())) {
                        eventPlace = childNode.getTextContent();
                    } else if (childNode.getNodeName().equals(XmlElementNames.EVENT_X.getXmlElementName())) {
                        eventX = childNode.getTextContent();
                    } else if (childNode.getNodeName().equals(XmlElementNames.EVENT_Y.getXmlElementName())) {
                        eventY = childNode.getTextContent();
                    } else if (childNode.getNodeName().equals(XmlElementNames.THUMBNAIL.getXmlElementName())) {
                        thumbnail = childNode.getTextContent();
                    } else if (childNode.getNodeName().equals(XmlElementNames.URL.getXmlElementName())) {
                        url = childNode.getTextContent();
                    }
                }
                cultureEventList.add(new CultureEvent(eventNm, eventPeriod, eventPlace, eventX, eventY, thumbnail, url, area));
            }

        }
        return cultureEventList;
    }

}
