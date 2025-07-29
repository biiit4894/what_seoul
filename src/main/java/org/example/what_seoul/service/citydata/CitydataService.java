package org.example.what_seoul.service.citydata;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.citydata.dto.*;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.citydata.population.Population;
import org.example.what_seoul.domain.citydata.population.PopulationForecast;
import org.example.what_seoul.domain.citydata.weather.Weather;
import org.example.what_seoul.repository.board.BoardRepository;
import org.example.what_seoul.repository.citydata.event.CultureEventRepository;
import org.example.what_seoul.repository.citydata.population.PopulationForecastRepository;
import org.example.what_seoul.repository.citydata.population.PopulationRepository;
import org.example.what_seoul.repository.citydata.weather.WeatherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class CitydataService {
    private final PopulationRepository populationRepository;
    private final PopulationForecastRepository populationForecastRepository;
    private final WeatherRepository weatherRepository;
    private final CultureEventRepository cultureEventRepository;
    private final BoardRepository boardRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public CommonResponse<ResGetPopulationDataDTO> findPopulationDataByAreaId(Long areaId) {
        Population population = populationRepository.findTopByAreaIdOrderByCreatedAtDesc(areaId).orElseThrow(() -> new EntityNotFoundException("인구 현황 데이터를 찾지 못했습니다."));

        return new CommonResponse<>(
                true,
                "장소별 인구 현황 데이터 조회 성공",
                ResGetPopulationDataDTO.from(population)
        );

    }

    @Transactional(readOnly = true)
    public CommonResponse<ResGetWeatherDataDTO> findWeatherDataByAreaId(Long areaId) {
        Weather weather = weatherRepository.findTopByAreaIdOrderByCreatedAtDesc(areaId).orElseThrow(() -> new EntityNotFoundException("날씨 현황 데이터를 찾지 못했습니다."));

        return new CommonResponse<>(
                true,
                "장소별 날씨 현황 데이터 조회 성공",
                ResGetWeatherDataDTO.from(weather)
        );

    }

    @Transactional(readOnly = true)
    public CommonResponse<List<ResGetCultureEventDataDTO>> findCultureEventDataByAreaId(Long areaId) {
        List<CultureEvent> cultureEventList = cultureEventRepository.findAllByAreaIdIsOrderByIsEndedAsc(areaId).orElseThrow(() -> new EntityNotFoundException("문화 행사 데이터를 찾지 못했습니다."));

        return new CommonResponse<>(
                true,
                "장소별 문화행사 데이터 조회 성공",
                ResGetCultureEventDataDTO.from(cultureEventList)
        );
    }

    /**
     * 주어진 인구 데이터, 인구 예측 데이터 및 날씨 데이터를 업데이트하는 메소드
     * - 먼저 기존의 데이터를 모두 삭제한 후, 새 데이터를 저장한다.
     *
     * @param populationList 인구 데이터를 포함하는 목록
     * @param populationForecastList 인구 예측 데이터를 포함하는 목록
     * @param weatherList 날씨 데이터를 포함하는 목록
     */
    @Transactional
    public void updatePopulationAndWeatherData(List<Population> populationList, List<PopulationForecast> populationForecastList, List<Weather> weatherList) {
//        populationForecastRepository.deleteAllInBatch();
//        populationRepository.deleteAllInBatch();
//        weatherRepository.deleteAllInBatch();

        populationRepository.saveAll(populationList);
        populationForecastRepository.saveAll(populationForecastList);
        weatherRepository.saveAll(weatherList);
    }

    /**
     * 문화행사 데이터를 업데이트하거나 새로 삽입하는 메소드
     * 1. 데이터베이스에 존재하는 모든 행사에 대해 `isEnded` 필드를 갱신한다. 해당 행사 종료 여부는 `evaluateIsEnded` 메소드를 통해 평가힌디/
     * 2. 새로 가져온 `CultureEvent` 목록에 대해 각 문화행사 이름과 관련된 장소(Area)가 동일한 기존 행사가 있는지 확인한다.
     *    - 기존 행사가 있으면 `newCultureEvent`의 데이터로 기존 행사를 업데이트한다.
     *    - 기존 행사가 없으면 새 행사로 데이터베이스에 저장한다.
     * 3. `isEnded` 필드가 갱신되었거나 `updateFrom` 메소드로 정보가 변경된 행사들을 데이터베이스에 저장한다.
     *
     * @param fetchedEvents 새로 가져온 문화행사 목록을 처리하여 데이터베이스에 업데이트하거나 삽입한다.
     */
    @Transactional
    public void updateOrInsertCultureEventData(List<CultureEvent> fetchedEvents) {
        List<CultureEvent> allEvents = cultureEventRepository.findAll();

        // 행사 종료 여부를 판단해 isEnded 필드 값 갱신
        List<CultureEvent> isEndedUpdatedEvents = new ArrayList<>();
        for (CultureEvent event : allEvents) {
            if (event.updateIsEnded(event.evaluateIsEnded())) {
                isEndedUpdatedEvents.add(event);
            }
        }
        cultureEventRepository.saveAll(isEndedUpdatedEvents); // isEnded값이 갱신된 행사들만 변경사항을 DB에 반영


        List<CultureEvent> updatedEvents = new ArrayList<>();
        for (CultureEvent newCultureEvent : fetchedEvents) {
            Optional<CultureEvent> existingCultureEvent = cultureEventRepository.findByEventNameAndArea(newCultureEvent.getEventName(), newCultureEvent.getArea());

            // 문화행사 이름과 관련 장소(Area)가 동일할 경우 새롭게 fetch된 문화행사 정보로 갱신
            if (existingCultureEvent.isPresent()) {
                log.info("event {} is present", newCultureEvent.getEventName());
                CultureEvent existing = existingCultureEvent.get();
                if(existing.updateFrom(newCultureEvent)) {
                    updatedEvents.add(existing);
                }
            } else {
                log.info("event {} is new", newCultureEvent.getEventName());
                cultureEventRepository.save(newCultureEvent); // 아예 새로운 행사라면 DB에 새로 저장
            }
        }

        cultureEventRepository.saveAll(updatedEvents); // 정보가 갱신된(updateFrom된) 행사들만 변경사항을 DB에 반영

    }

    /**
     * 각 area별 population 데이터 중에서
     * 최신 5개의 population만 유지하는 상황에서 그 외 population과 연결된 forecast 데이터를 삭제한다.
     *
     * population 삭제 전에 forecast 데이터를 먼저 삭제한다.
     */
    @Transactional
    public void deleteOldPopulationForecastData() {
        String sql = """
                       DELETE pf
                         FROM population_forecast pf
                         LEFT JOIN (
                             SELECT id
                             FROM (
                                 SELECT id,
                                        ROW_NUMBER() OVER (PARTITION BY area_id ORDER BY created_at DESC) AS rn
                                 FROM population
                             ) ranked
                             WHERE rn <= 5
                         ) keep ON pf.population_id = keep.id
                         WHERE keep.id IS NULL;
                         
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.executeUpdate();
    }

    /**
     * 각 area별 population 데이터 중에서
     * forecast와 연결되지 않고, 최신 5개에도 포함되지 않은 population만 삭제한다.
     *
     * (외래 키 제약 조건으로 인해 forecast가 남아 있는 population은 삭제할 수 없음)
     */
    @Transactional
    public void deleteOldPopulationData() {
        String sql = """
                DELETE p
                 FROM population p
                 LEFT JOIN population_forecast pf ON pf.population_id = p.id
                 LEFT JOIN (
                     SELECT id
                     FROM (
                         SELECT id,
                                ROW_NUMBER() OVER (PARTITION BY area_id ORDER BY created_at DESC) AS rn
                         FROM population
                     ) ranked
                     WHERE rn <= 5
                 ) keep ON p.id = keep.id
                 WHERE pf.id IS NULL AND keep.id IS NULL;
                          
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.executeUpdate();
    }

    /**
     * 각 area별 weather 데이터 중에서
     * 최신 5개의 데이터를 제외한 나머지를 모두 삭제한다.
     */
    @Transactional
    public void deleteOldWeatherData() {
        String sql = """
                        DELETE FROM weather
                        WHERE id IN (
                            SELECT id FROM (
                                SELECT id,
                                    ROW_NUMBER() OVER (PARTITION BY area_id ORDER BY created_at DESC) AS rn
                                FROM weather
                            ) AS sub
                            WHERE rn > 5
                        )
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.executeUpdate();
    }


    /**
     * 리뷰가 없는 만료된 문화행사를 삭제하는 메소드.
     * 이 메소드는 다음 조건에 맞는 문화행사를 식별한다.
     * 1. `isEnded` 필드가 true인 행사.
     * 2. `endDate`가 현재 날짜로부터 3개월 이상 지난 행사.
     * 그런 후, `boardRepository`를 사용하여 해당 행사에 관련된 리뷰가 있는지 확인한다.
     * 리뷰가 없는 만료된 행사만 삭제 대상이 된다.
     */
    @Transactional
    public void deleteExpiredCultureEventsWithoutReviews() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(3);
        // 종료된 문화행사 전체
        List<CultureEvent> endedEvents = cultureEventRepository.findAllByIsEndedTrue();

        // 행사 종료일 기준으로 3개월을 넘게 지났는지 비교
        List<CultureEvent> candidates = endedEvents.stream()
                .filter(event -> {
                    String period = event.getEventPeriod();
                    if (period != null && period.contains("~")) {
                        try {
                            String endDateStr = period.split("~")[1].trim(); // eventPeriod에서의 종료일 부분
                            LocalDate endDate = LocalDate.parse(endDateStr); // "YYYY-MM-DD" 형태로 파싱
                            return endDate.atStartOfDay().isBefore(cutoff);
                        } catch (Exception e) {
                            log.warn("Failed to parse eventPeriod: {}", period); // 파싱 실패한 경우는 삭제 대상에서 제외
                            return false;
                        }
                    }
                    return false;
                })
                .toList();

        // 후기가 없는 행사만 삭제
        List<CultureEvent> deletable = candidates.stream()
                .filter(e -> !boardRepository.existsByCultureEventId(e.getId()))
                .toList();

        cultureEventRepository.deleteAll(deletable);
    }
}
