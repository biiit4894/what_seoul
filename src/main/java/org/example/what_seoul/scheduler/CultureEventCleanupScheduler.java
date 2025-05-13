package org.example.what_seoul.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.what_seoul.service.citydata.CitydataService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CultureEventCleanupScheduler {
    private final CitydataService citydataService;

    /**
     * 매일 03시에, 종료된 지 3개월 이상 경과했고, 후기가 없는 문화행사를 삭제한다.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteExpiredCultureEvents() {
        citydataService.deleteExpiredCultureEventsWithoutReviews();
    }
}
