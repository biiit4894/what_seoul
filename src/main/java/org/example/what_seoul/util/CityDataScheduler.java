package org.example.what_seoul.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.population.Population;
import org.example.what_seoul.domain.citydata.population.PopulationForecast;
import org.example.what_seoul.domain.citydata.weather.Weather;
import org.example.what_seoul.repository.citydata.AreaRepository;
import org.example.what_seoul.repository.citydata.population.PopulationForecastRepository;
import org.example.what_seoul.repository.citydata.population.PopulationRepository;
import org.example.what_seoul.repository.citydata.weather.WeatherRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CityDataScheduler {
    private final AreaRepository areaRepository;
    private final PopulationRepository populationRepository;
    private final PopulationForecastRepository populationForecastRepository;
    private final WeatherRepository weatherRepository;

    @Value("${seoul.open.api.url}")
    private String url;

    //@Transactional
    @Scheduled(fixedDelay = 5 * 60 * 1000) // 이전 실행이 끝난 후 5분 뒤에 다음 실행
    public void call() {
        LocalDateTime beforeTime = LocalDateTime.now();

        // 서울시내 핫스팟 장소 116곳 조회
        List<Area> areas = areaRepository.findAll();

        List<Population> populationList = new ArrayList<>();
        List<PopulationForecast> populationForecastList = new ArrayList<>();
        List<Weather> weatherList = new ArrayList<>();

        for (Area area : areas) {
            // 각 장소에 대한 도시데이터 fetch
            Document document = fetchCityData(area);
            if (document == null) {
                log.warn("skipping area {} because document is null", area.getAreaName());
                continue;
            }

            // 인구 데이터 파싱
            Population population = parsePopulationData(document, area);
            populationList.add(population);

            // 인구 예측 데이터 파싱
            List<PopulationForecast> forecastList = parsePopulationForecastData(document, population);
            populationForecastList.addAll(forecastList);

            // 날씨 데이터 파싱
            Weather weather = parseWeatherData(document, area);
            weatherList.add(weather);

        }

        populationRepository.deleteAll();
        populationForecastRepository.deleteAll();
        weatherRepository.deleteAll();

        populationRepository.saveAll(populationList);
        populationForecastRepository.saveAll(populationForecastList);
        weatherRepository.saveAll(weatherList);


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
        return new Population(
                getElementTextContent(document, XmlElementNames.AREA_CONGEST_LVL.getXmlElementName()),
                getElementTextContent(document, XmlElementNames.AREA_CONGEST_MSG.getXmlElementName()),
                getElementTextContent(document, XmlElementNames.AREA_PPLTN_MIN.getXmlElementName()),
                getElementTextContent(document, XmlElementNames.AREA_PPLTN_MAX.getXmlElementName()),
                getElementTextContent(document, XmlElementNames.PPLTN_TIME.getXmlElementName()),
                area
        );
    }

    /**
     * XML 문서에서 인구 예측 데이터를 파싱하여 PopulationForecast 객체 리스트 생성
     * @param document
     * @param population
     * @return
     */
    private List<PopulationForecast> parsePopulationForecastData(Document document, Population population) {
        List<PopulationForecast> forecastList = new ArrayList<>();
        NodeList nodeList = document.getElementsByTagName(XmlElementNames.FCST_PPLTN.getXmlElementName());
        for (int i = 1; i < nodeList.getLength(); i++) {
            Node fcstPpltnNode = nodeList.item(i);
            NodeList childNodes = fcstPpltnNode.getChildNodes();

            String forecastCongetstionLevel = "No Tag";
            String forecastPpltnMin = "No Tag";
            String forecastPpltnMax = "No Tag";
            String forecasePplTime = "No Tag";

            for (int j = 0; j < childNodes.getLength(); j++) {
                Node childNode = childNodes.item(j);
                if (childNode.getNodeName().equals(XmlElementNames.FCST_CONGEST_LVL.getXmlElementName())) {
                    forecastCongetstionLevel = childNode.getTextContent();
                } else if (childNode.getNodeName().equals(XmlElementNames.FCST_PPLTN_MIN.getXmlElementName())) {
                    forecastPpltnMin = childNode.getTextContent();
                } else if (childNode.getNodeName().equals(XmlElementNames.FCST_PPLTN_MAX.getXmlElementName())) {
                    forecastPpltnMax = childNode.getTextContent();
                } else if (childNode.getNodeName().equals(XmlElementNames.FCST_TIME.getXmlElementName())) {
                    forecasePplTime = childNode.getTextContent();
                }
            }

            forecastList.add(new PopulationForecast(
                    forecastCongetstionLevel,
                    forecastPpltnMin,
                    forecastPpltnMax,
                    forecasePplTime,
                    population));

        }
        return forecastList;
    }

    /**
     * XML 문서에서 날씨 데이터를 파싱하여 PopulationForecast 객체 리스트 생성
     * @param document
     * @param area
     * @return
     */
    private Weather parseWeatherData(Document document, Area area) {
        String temperature = "No Tag";
        String maxTemperature = "No Tag";
        String minTemperature = "No Tag";
        String pm25Index = "No Tag";
        String pm25 = "No Tag";
        String pm10Index = "No Tag";
        String pm10 = "No Tag";
        String pcpMsg = "No Tag";
        String weatherUpdateTime = "No Tag";

        NodeList weatherNodeList = document.getElementsByTagName(XmlElementNames.WEATHER_STTS.getXmlElementName());
        Node weatherParentNode = weatherNodeList.item(1);
        if (weatherParentNode != null) {
            NodeList childNodeList = weatherParentNode.getChildNodes();


            for (int i = 0; i < childNodeList.getLength(); i++) {
                Node childNode = childNodeList.item(i);

                if (childNode.getNodeName().equals(XmlElementNames.TEMP.getXmlElementName())) {
                    temperature = childNode.getTextContent();
                } else if (childNode.getNodeName().equals(XmlElementNames.MAX_TEMP.getXmlElementName())) {
                    maxTemperature = childNode.getTextContent();
                } else if (childNode.getNodeName().equals(XmlElementNames.MIN_TEMP.getXmlElementName())) {
                    minTemperature = childNode.getTextContent();
                } else if (childNode.getNodeName().equals(XmlElementNames.PM25_INDEX.getXmlElementName())) {
                    pm25Index = childNode.getTextContent();
                } else if (childNode.getNodeName().equals(XmlElementNames.PM25.getXmlElementName())) {
                    pm25 = childNode.getTextContent();
                } else if (childNode.getNodeName().equals(XmlElementNames.PM10_INDEX.getXmlElementName())) {
                    pm10Index = childNode.getTextContent();
                } else if (childNode.getNodeName().equals(XmlElementNames.PM10.getXmlElementName())) {
                    pm10 = childNode.getTextContent();
                } else if (childNode.getNodeName().equals(XmlElementNames.PCP_MSG.getXmlElementName())) {
                    pcpMsg = childNode.getTextContent();
                } else if (childNode.getNodeName().equals(XmlElementNames.WEATHER_TIME.getXmlElementName())) {
                    weatherUpdateTime = childNode.getTextContent();
                }
            }
        }

        return new Weather(
                temperature,
                maxTemperature,
                minTemperature,
                pm25Index,
                pm25,
                pm10Index,
                pm10,
                pcpMsg,
                weatherUpdateTime,
                area
        );
    }


    private String getElementTextContent(Document document, String xmlTagName) {
        NodeList nodeList = document.getElementsByTagName(xmlTagName);

        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "No Tag";
    }


}
