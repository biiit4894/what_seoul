package org.example.what_seoul.service.citydata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.citydata.population.Population;
import org.example.what_seoul.domain.citydata.population.PopulationForecast;
import org.example.what_seoul.domain.citydata.weather.Weather;
import org.example.what_seoul.exception.CitydataSchedulerException;
import org.example.what_seoul.util.XmlElementNames;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CitydataParser {
    private final PcpMsgHistoryService pcpMsgHistoryService;

    private static final String NO_TAG = "정보가 없습니다.";

    /**
     * XML 문서에서 인구 데이터를 파싱하여 Population 객체 생성
     * @param document
     * @param area
     * @return Population
     */
    public Population parsePopulationData(Document document, Area area) {
        try {
            NodeList populationNodeList = document.getElementsByTagName(XmlElementNames.LIVE_PPLTN_STTS.getXmlElementName());

            String congestionLevel = NO_TAG;
            String congestionMessage = NO_TAG;
            String minPopulation = NO_TAG;
            String maxPopulation = NO_TAG;
            String populationUpdateTime = NO_TAG;

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
        } catch (Exception e) {
            log.error("failed to parse city data for area {}: {}", area.getAreaName(), e.getMessage());
            throw new CitydataSchedulerException(area.getAreaName() + "의 인구 현황 데이터 파싱에 실패했습니다.");
        }

    }

    /**
     * XML 문서에서 인구 예측 데이터를 파싱하여 PopulationForecast 객체 리스트 생성
     *
     * @param document
     * @param population
     * @param area
     * @return List<PopulationForecast>
     */
    public List<PopulationForecast> parsePopulationForecastData(Document document, Population population, Area area) {
        try {
            List<PopulationForecast> forecastList = new ArrayList<>();
            NodeList nodeList = document.getElementsByTagName(XmlElementNames.FCST_PPLTN.getXmlElementName());

            String forecastCongestionLevel = NO_TAG;
            String forecastPpltnMin = NO_TAG;
            String forecastPpltnMax = NO_TAG;
            String forecasePplTime = NO_TAG;

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
        } catch(Exception e) {
            log.error("failed to parse city data for area {}: {}", area.getAreaName(), e.getMessage());
            throw new CitydataSchedulerException(area.getAreaName() + "의 인구 예측값 데이터 파싱에 실패했습니다.");
        }
    }

    /**
     * XML 문서에서 날씨 데이터를 파싱하여 PopulationForecast 객체 리스트 생성
     *
     * @param document
     * @param area
     * @return Weather
     */
    public Weather parseWeatherData(Document document, Area area) {
        try {
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
                    weatherData.getOrDefault(XmlElementNames.TEMP, NO_TAG),
                    weatherData.getOrDefault(XmlElementNames.MAX_TEMP, NO_TAG),
                    weatherData.getOrDefault(XmlElementNames.MIN_TEMP, NO_TAG),
                    weatherData.getOrDefault(XmlElementNames.PM25_INDEX, NO_TAG),
                    weatherData.getOrDefault(XmlElementNames.PM25, NO_TAG),
                    weatherData.getOrDefault(XmlElementNames.PM10_INDEX, NO_TAG),
                    weatherData.getOrDefault(XmlElementNames.PM10, NO_TAG),
                    weatherData.getOrDefault(XmlElementNames.PCP_MSG, NO_TAG),
                    weatherData.getOrDefault(XmlElementNames.WEATHER_TIME, NO_TAG),
                    area
            );
        } catch (Exception e) {
            log.error("failed to parse city data for area {}: {}", area.getAreaName(), e.getMessage());
            throw new CitydataSchedulerException(area.getAreaName() + "의 날씨 현황 데이터 파싱에 실패했습니다.");
        }

    }

    /**
     * XML 문서에서 문화행사 데이터를 파싱하여 CultureEvent 객체 리스트 생성
     * @param document
     * @param area
     * @return List<CultureEvent>
     */
    public List<CultureEvent> parseCultureEventData(Document document, Area area) {
        try {
            List<CultureEvent> cultureEventList = new ArrayList<>();
            NodeList cultureEventNodeList = document.getElementsByTagName(XmlElementNames.EVENT_STTS.getXmlElementName());
            String eventNm = NO_TAG;
            String eventPeriod = NO_TAG;
            String eventPlace = NO_TAG;
            String eventX = NO_TAG;
            String eventY = NO_TAG;
            String thumbnail = NO_TAG;
            String url = NO_TAG;

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
        } catch (Exception e) {
            log.error("failed to parse city data for area {}: {}", area.getAreaName(), e.getMessage());
            throw new CitydataSchedulerException(area.getAreaName() + "의 문화행사 데이터 파싱에 실패했습니다.");
        }

    }
}
