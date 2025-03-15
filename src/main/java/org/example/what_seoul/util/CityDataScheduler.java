package org.example.what_seoul.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.domain.cityData.Area;
import org.example.what_seoul.domain.cityData.population.Population;
import org.example.what_seoul.repository.cityData.AreaRepository;
import org.example.what_seoul.repository.cityData.population.PopulationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
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

    @Value("${seoul.open.api.url}")
    private String url;

    @Transactional
    public void call() throws ParserConfigurationException, IOException, SAXException {
        LocalDateTime beforeTime = LocalDateTime.now();

        List<Area> areas = areaRepository.findAll();
        List<Population> populationList = new ArrayList<>();

        for (Area area : areas) {
            String encodedAreaName = URLEncoder.encode(area.getAreaName(), StandardCharsets.UTF_8);
            log.info("        area name: " + area.getAreaName());
            log.info("encoded area name: " + encodedAreaName);
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url + encodedAreaName);
            document.getDocumentElement().normalize();

            Population population = new Population(
                    getElement(document, XmlElementNames.AREA_CONGEST_LVL.getElementName()),
                    getElement(document, XmlElementNames.AREA_CONGEST_MSG.getElementName()),
                    getElement(document, XmlElementNames.AREA_PPLTN_MIN.getElementName()),
                    getElement(document, XmlElementNames.AREA_PPLTN_MAX.getElementName()),
                    getElement(document, XmlElementNames.PPLTN_TIME.getElementName())
            );

            populationList.add(population);
            populationRepository.saveAll(populationList);
        }

        LocalDateTime afterTime = LocalDateTime.now();
        log.info("호출 시작 시간 = " + beforeTime);
        log.info("호출 종료 시간 = " + afterTime);

        long totalTime = java.time.Duration.between(beforeTime, afterTime).getSeconds();
        log.info("소요 시간 = " + totalTime + " 초");
    }


    private String getElement(Document document, String xmlTagName) {
        NodeList nodeList = document.getElementsByTagName(xmlTagName);

        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "No Tag";
    }


}
