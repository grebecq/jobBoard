package fedoseev.jobboard.aggregator;

import fedoseev.jobboard.entity.Skill;
import fedoseev.jobboard.exception.DuplicateResourceException;
import fedoseev.jobboard.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/** Обходит все источники и складывает их вакансии в базу. */
@Slf4j
@Service
@RequiredArgsConstructor
public class VacancyImportService {

    private final List<VacancySourceClient> clients;
    private final ImportedVacancyWriter writer;
    private final SkillRepository skillRepository;
    private final AggregatorProperties properties;

    // расписание и ручной запуск не должны идти одновременно
    private final ReentrantLock lock = new ReentrantLock();

    public List<ImportResult> importAll() {
        if (!lock.tryLock()) {
            throw new DuplicateResourceException("Сбор вакансий уже идёт");
        }
        try {
            List<ImportResult> results = new ArrayList<>();
            for (VacancySourceClient client : clients) {
                results.add(importFrom(client));
            }
            return results;
        } finally {
            lock.unlock();
        }
    }

    ImportResult importFrom(VacancySourceClient client) {
        LocalDateTime startedAt = LocalDateTime.now();
        List<ExternalVacancy> fetched;
        try {
            fetched = client.fetchLatest();
        } catch (RuntimeException e) {
            log.error("{}: сбор не удался: {}", client.source(), e.getMessage());
            return ImportResult.failed(client.source(), e.getMessage());
        }

        List<Skill> skills = skillRepository.findAll();
        int created = 0;
        int updated = 0;
        int detailsLeft = properties.getMaxDetailsPerRun();

        for (ExternalVacancy ext : fetched) {
            try {
                if (writer.exists(client.source(), ext.externalId())) {
                    writer.touch(client.source(), ext, startedAt);
                    updated++;
                    continue;
                }
                ExternalVacancy full = ext;
                if (!ext.detailed() && detailsLeft > 0) {
                    detailsLeft--;
                    full = client.fetchDetails(ext);
                }
                writer.create(client.source(), full, skills, startedAt);
                created++;
            } catch (RuntimeException e) {
                log.warn("{}: вакансия {} пропущена: {}", client.source(), ext.externalId(), e.getMessage());
            }
        }

        int closed = writer.closeStale(client.source(), startedAt.minusDays(properties.getStaleDays()));
        log.info("{}: получено {}, новых {}, обновлено {}, закрыто {}", client.source(), fetched.size(), created, updated, closed);
        return new ImportResult(client.source(), fetched.size(), created, updated, closed, null);
    }
}
