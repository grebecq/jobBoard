package fedoseev.jobboard.aggregator;

import fedoseev.jobboard.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "aggregator", name = "enabled", havingValue = "true")
public class AggregatorScheduler {

    private final VacancyImportService importService;
    private final AggregatorProperties properties;

    @Scheduled(cron = "${aggregator.cron:0 0 */3 * * *}")
    public void scheduled() {
        run();
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        if (properties.isRunOnStartup()) {
            run();
        }
    }

    private void run() {
        try {
            importService.importAll();
        } catch (DuplicateResourceException e) {
            log.info(e.getMessage());
        }
    }
}
