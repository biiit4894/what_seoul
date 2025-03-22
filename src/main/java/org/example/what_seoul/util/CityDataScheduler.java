package org.example.what_seoul.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.population.Population;
import org.example.what_seoul.domain.citydata.population.PopulationForecast;
import org.example.what_seoul.repository.cityData.AreaRepository;
import org.example.what_seoul.repository.cityData.population.PopulationForecastRepository;
import org.example.what_seoul.repository.cityData.population.PopulationRepository;
import org.example.what_seoul.service.citydata.CityDataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
    private final CityDataService cityDataService;

    @Value("${seoul.open.api.url}")
    private String url;

    @Transactional
    public void call() {
        LocalDateTime beforeTime = LocalDateTime.now();

        List<Area> areas = areaRepository.findAll();
        List<Population> populationList = new ArrayList<>();

        try {
            for (Area area : areas) {
                String sanitizedAreaName = area.getAreaName().replace("&", "&amp;");
                String encodedAreaName = URLEncoder.encode(sanitizedAreaName, StandardCharsets.UTF_8);
                log.info("sanitized area name: " + sanitizedAreaName);
                log.info("encoded area name: " + encodedAreaName);
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url + encodedAreaName);
                document.getDocumentElement().normalize();

                Population population = new Population(
                        getElementTextContent(document, XmlElementNames.AREA_CONGEST_LVL.getXmlElementName()),
                        getElementTextContent(document, XmlElementNames.AREA_CONGEST_MSG.getXmlElementName()),
                        getElementTextContent(document, XmlElementNames.AREA_PPLTN_MIN.getXmlElementName()),
                        getElementTextContent(document, XmlElementNames.AREA_PPLTN_MAX.getXmlElementName()),
                        getElementTextContent(document, XmlElementNames.PPLTN_TIME.getXmlElementName())
                );

                populationList.add(population);
                populationRepository.saveAll(populationList);

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

                populationForecastRepository.saveAll(forecastList);
            }

            LocalDateTime afterTime = LocalDateTime.now();
            log.info("호출 시작 시간 = " + beforeTime);
            log.info("호출 종료 시간 = " + afterTime);

            long totalTime = java.time.Duration.between(beforeTime, afterTime).getSeconds();
            log.info("소요 시간 = " + totalTime + " 초");
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("error while fetching cityData");
            log.error("exception message : {}", e.getMessage());
        }


    }


    private String getElementTextContent(Document document, String xmlTagName) {
        NodeList nodeList = document.getElementsByTagName(xmlTagName);

        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "No Tag";
    }


}
