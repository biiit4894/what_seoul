package org.example.what_seoul.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.what_seoul.service.citydata.CitydataService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CitydataCleanupScheduler {
    private final CitydataService citydataService;

    /**
     * 매 시간의 2분, 17분, 32분, 47분에 실행
     * 예측 인구 현황, 실시간 인구 현황, 날씨 데이터를 정기적으로 삭제
     */

    @Scheduled(cron = "0 2,17,32,47 * * * *")
    @Transactional
    public void deleteOldPopulationAndWeatherData() {
        citydataService.deleteOldPopulationForecastData();
        citydataService.deleteOldPopulationData();
        citydataService.deleteOldWeatherData();
    }


    /**
     * 매일 03시에, 종료된 지 3개월 이상 경과했고, 후기가 없는 문화행사를 삭제한다.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteExpiredCultureEvents() {
        citydataService.deleteExpiredCultureEventsWithoutReviews();
    }
}
