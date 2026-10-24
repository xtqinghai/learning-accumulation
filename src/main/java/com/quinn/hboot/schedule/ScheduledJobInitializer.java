package com.quinn.hboot.schedule;

import com.quinn.hboot.schedule.service.ScheduledJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledJobInitializer {

    private final ScheduledJobService scheduledJobService;

    @EventListener(ApplicationReadyEvent.class)
    public void restoreJobs() {
        scheduledJobService.restoreActiveJobs();
        log.info("Managed scheduled jobs restored");
    }
}
