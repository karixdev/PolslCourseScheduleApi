package com.github.karixdev.scheduleservice.application.strategy.blankupdate;

import com.github.karixdev.scheduleservice.application.event.ScheduleEvent;
import com.github.karixdev.scheduleservice.application.event.producer.EventProducer;
import com.github.karixdev.scheduleservice.application.pagination.Page;
import com.github.karixdev.scheduleservice.application.pagination.PageRequest;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class BlankAllSchedulesUpdateStrategy extends BlankSchedulesUpdateStrategy {

    private final ScheduleRepository repository;

    private static final int PAGE_SIZE = 10;

    public BlankAllSchedulesUpdateStrategy(EventProducer<ScheduleEvent> eventProducer, ScheduleRepository repository) {
        super(eventProducer);
        this.repository = repository;
    }

    @Override
    public boolean supports(List<UUID> ids) {
        return ids.isEmpty();
    }

    @Override
    public void blankUpdate(List<UUID> ids) {
        int page = 0;

        while (true) {
            PageRequest pageRequest = new PageRequest(page, PAGE_SIZE);
            Page<Schedule> schedulesPage = repository.findAllPaginated(pageRequest);

            publishEvents(schedulesPage.content());

            page++;

            if (Boolean.TRUE.equals(schedulesPage.pageInfo().isLast())) {
                break;
            }
        }

    }
}
