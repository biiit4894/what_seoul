package org.example.what_seoul.scheduler;

import org.example.what_seoul.config.WebSecurityTestWithH2Config;
import org.example.what_seoul.service.citydata.CitydataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test-h2")
@Import(WebSecurityTestWithH2Config.class)
@Transactional
public class CultureEventCleanupSchedulerTest {
    @Mock
    private CitydataService citydataService;

    @InjectMocks
    private CultureEventCleanupScheduler cultureEventCleanupScheduler;

    @Test
    @DisplayName("[성공] 문화행사 삭제 스케줄러 - deleteExpiredCultureEvents")
    void deleteExpiredCultureEvents() {
        // when
        cultureEventCleanupScheduler.deleteExpiredCultureEvents();

        // then
        verify(citydataService, times(1)).deleteExpiredCultureEventsWithoutReviews();
    }
}
