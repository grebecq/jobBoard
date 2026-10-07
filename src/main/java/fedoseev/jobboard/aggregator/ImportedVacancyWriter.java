package fedoseev.jobboard.aggregator;

import fedoseev.jobboard.entity.Company;
import fedoseev.jobboard.entity.Skill;
import fedoseev.jobboard.entity.Vacancy;
import fedoseev.jobboard.enums.VacancySource;
import fedoseev.jobboard.enums.VacancyStatus;
import fedoseev.jobboard.repository.CompanyRepository;
import fedoseev.jobboard.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Сохраняет вакансии из источников. Каждая вакансия в своей транзакции, чтобы одна ошибка не роняла весь сбор. */
@Component
@RequiredArgsConstructor
public class ImportedVacancyWriter {

    private static final int TITLE_MAX = 255;

    private final VacancyRepository vacancyRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public boolean exists(VacancySource source, String externalId) {
        return vacancyRepository.existsBySourceAndExternalId(source, externalId);
    }

    /** Вакансия уже есть: отмечаем, что она всё ещё висит на сайте, и обновляем то, что могло поменяться. */
    @Transactional
    public void touch(VacancySource source, ExternalVacancy ext, LocalDateTime seenAt) {
        vacancyRepository.findBySourceAndExternalId(source, ext.externalId()).ifPresent(v -> {
            v.setLastSeenAt(seenAt);
            v.setStatus(VacancyStatus.ACTIVE);
            v.setTitle(cut(ext.title()));
            v.setSalaryFrom(ext.salaryFrom());
            v.setSalaryTo(ext.salaryTo());
            if (ext.publishedAt() != null) {
                v.setPublishedAt(ext.publishedAt());
            }
        });
    }

    @Transactional
    public void create(VacancySource source, ExternalVacancy ext, List<Skill> skillDictionary, LocalDateTime seenAt) {
        Vacancy v = new Vacancy();
        v.setSource(source);
        v.setExternalId(ext.externalId());
        v.setExternalUrl(ext.url());
        v.setTitle(cut(ext.title()));
        v.setDescription(ext.description() == null || ext.description().isBlank()
                ? "Подробное описание на сайте источника." : ext.description());
        v.setCity(ext.city());
        v.setSalaryFrom(ext.salaryFrom());
        v.setSalaryTo(ext.salaryTo());
        v.setWorkFormat(ext.workFormat());
        v.setEmploymentType(ext.employmentType());
        v.setExperience(ext.experience());
        v.setGrade(VacancyClassifier.grade(ext.title(), ext.experience()));
        v.setSpecialization(VacancyClassifier.specialization(ext.title()));
        v.setSkills(SkillMatcher.match(skillDictionary, ext.skillNames(), ext.title(), ext.description()));
        v.setPublishedAt(ext.publishedAt() != null ? ext.publishedAt() : seenAt);
        v.setLastSeenAt(seenAt);
        v.setCompany(company(source, ext));
        vacancyRepository.save(v);
    }

    /** Закрывает вакансии источника, которых давно нет в выдаче. */
    @Transactional
    public int closeStale(VacancySource source, LocalDateTime seenBefore) {
        return vacancyRepository.closeNotSeenSince(source, seenBefore, VacancyStatus.ACTIVE, VacancyStatus.CLOSED);
    }

    private Company company(VacancySource source, ExternalVacancy ext) {
        // у анонимных работодателей нет id, тогда различаем их по названию
        String externalId = ext.companyExternalId() != null ? ext.companyExternalId() : "name:" + ext.companyName();
        Optional<Company> existing = companyRepository.findBySourceAndExternalId(source, externalId);
        if (existing.isPresent()) {
            Company c = existing.get();
            if (ext.companyLogoUrl() != null) {
                c.setLogoUrl(ext.companyLogoUrl());
            }
            return c;
        }
        Company c = new Company();
        c.setSource(source);
        c.setExternalId(cut(externalId));
        c.setName(cut(ext.companyName()));
        c.setLogoUrl(ext.companyLogoUrl());
        return companyRepository.save(c);
    }

    private static String cut(String s) {
        return s == null || s.length() <= TITLE_MAX ? s : s.substring(0, TITLE_MAX);
    }
}
