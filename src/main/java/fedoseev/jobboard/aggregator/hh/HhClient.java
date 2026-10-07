package fedoseev.jobboard.aggregator.hh;

import fedoseev.jobboard.aggregator.AggregatorProperties;
import fedoseev.jobboard.aggregator.ExternalVacancy;
import fedoseev.jobboard.aggregator.HtmlText;
import fedoseev.jobboard.aggregator.VacancySourceClient;
import fedoseev.jobboard.enums.EmploymentType;
import fedoseev.jobboard.enums.Experience;
import fedoseev.jobboard.enums.VacancySource;
import fedoseev.jobboard.enums.WorkFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/** Вакансии с hh.ru через их открытый API. */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "aggregator.hh", name = "enabled", havingValue = "true", matchIfMissing = true)
public class HhClient implements VacancySourceClient {

    // hh.ru пишет пояс без двоеточия: 2026-10-06T12:00:00+0300
    private static final DateTimeFormatter HH_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    private final AggregatorProperties.Hh props;
    private final RestClient rest;

    public HhClient(RestClient.Builder builder, AggregatorProperties properties) {
        this.props = properties.getHh();
        RestClient.Builder b = builder.clone()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("HH-User-Agent", props.getUserAgent())
                .defaultHeader("User-Agent", props.getUserAgent());
        if (props.getToken() != null && !props.getToken().isBlank()) {
            b.defaultHeader("Authorization", "Bearer " + props.getToken());
        }
        this.rest = b.build();
    }

    @Override
    public VacancySource source() {
        return VacancySource.HH;
    }

    @Override
    public List<ExternalVacancy> fetchLatest() {
        List<ExternalVacancy> result = new ArrayList<>();
        int maxPages = Math.min(props.getMaxPages(), 2000 / props.getPerPage());
        for (int page = 0; page < maxPages; page++) {
            int p = page;
            HhDto.Page response;
            try {
                response = rest.get().uri(uri -> searchUri(uri, p)).retrieve().body(HhDto.Page.class);
            } catch (RestClientException e) {
                if (page == 0) {
                    throw e;
                }
                log.warn("hh.ru: не удалось загрузить страницу {}: {}", page, e.getMessage());
                break;
            }
            if (response == null || response.items() == null || response.items().isEmpty()) {
                break;
            }
            response.items().stream()
                    .filter(v -> !v.archived())
                    .map(v -> toExternal(v, false))
                    .forEach(result::add);
            if (page + 1 >= response.pages()) {
                break;
            }
            pause();
        }
        log.info("hh.ru: получено {} вакансий", result.size());
        return result;
    }

    @Override
    public ExternalVacancy fetchDetails(ExternalVacancy vacancy) {
        pause();
        HhDto.Vacancy full = rest.get().uri("/vacancies/{id}", vacancy.externalId()).retrieve().body(HhDto.Vacancy.class);
        return full == null ? vacancy : toExternal(full, true);
    }

    private URI searchUri(UriBuilder uri, int page) {
        uri.path("/vacancies")
                .queryParam("text", props.getText())
                .queryParam("search_field", "name")
                .queryParam("order_by", "publication_time")
                .queryParam("period", props.getPeriodDays())
                .queryParam("per_page", props.getPerPage())
                .queryParam("page", page);
        props.getProfessionalRoles().forEach(r -> uri.queryParam("professional_role", r));
        props.getAreas().forEach(a -> uri.queryParam("area", a));
        return uri.build();
    }

    ExternalVacancy toExternal(HhDto.Vacancy v, boolean detailed) {
        HhDto.Salary salary = v.salaryRange() != null ? v.salaryRange() : v.salary();
        // в базе храним зарплату в рублях, вилку в валюте не показываем
        boolean rub = salary != null && "RUR".equals(salary.currency());
        HhDto.Employer employer = v.employer();

        String description = detailed
                ? HtmlText.toPlainText(v.description())
                : snippetText(v.snippet());

        return ExternalVacancy.builder()
                .externalId(v.id())
                .url(v.alternateUrl() != null ? v.alternateUrl() : "https://hh.ru/vacancy/" + v.id())
                .title(v.name())
                .description(description)
                .companyExternalId(employer == null ? null : employer.id())
                .companyName(employer == null || employer.name() == null ? "Компания не указана" : employer.name())
                .companyLogoUrl(employer == null || employer.logoUrls() == null ? null : employer.logoUrls().get("240"))
                .city(v.area() == null ? null : v.area().name())
                .salaryFrom(rub ? salary.from() : null)
                .salaryTo(rub ? salary.to() : null)
                .workFormat(workFormat(v))
                .employmentType(employmentType(v))
                .experience(experience(v.experience()))
                .skillNames(v.keySkills() == null ? List.of() : v.keySkills().stream().map(HhDto.Named::name).toList())
                .publishedAt(parseDate(v.publishedAt()))
                .detailed(detailed)
                .build();
    }

    private static String snippetText(HhDto.Snippet snippet) {
        if (snippet == null) {
            return "";
        }
        return Stream.of(snippet.responsibility(), snippet.requirement())
                .filter(Objects::nonNull)
                .map(HtmlText::toPlainText)
                .filter(s -> !s.isBlank())
                .reduce((a, b) -> a + "\n\n" + b)
                .orElse("");
    }

    private static WorkFormat workFormat(HhDto.Vacancy v) {
        if (v.workFormat() != null) {
            List<String> ids = v.workFormat().stream().map(HhDto.Named::id).toList();
            if (ids.contains("REMOTE")) return WorkFormat.REMOTE;
            if (ids.contains("HYBRID")) return WorkFormat.HYBRID;
            if (ids.contains("ON_SITE")) return WorkFormat.OFFICE;
        }
        if (v.schedule() != null && "remote".equals(v.schedule().id())) {
            return WorkFormat.REMOTE;
        }
        return v.schedule() == null ? null : WorkFormat.OFFICE;
    }

    private static EmploymentType employmentType(HhDto.Vacancy v) {
        if (v.employmentForm() != null && v.employmentForm().id() != null) {
            switch (v.employmentForm().id()) {
                case "FULL": return EmploymentType.FULL_TIME;
                case "PART": return EmploymentType.PART_TIME;
                case "PROJECT": return EmploymentType.PROJECT;
                default: break;
            }
        }
        if (v.employment() == null || v.employment().id() == null) {
            return null;
        }
        return switch (v.employment().id()) {
            case "full" -> EmploymentType.FULL_TIME;
            case "part" -> EmploymentType.PART_TIME;
            case "project" -> EmploymentType.PROJECT;
            case "probation" -> EmploymentType.INTERNSHIP;
            default -> null;
        };
    }

    private static Experience experience(HhDto.Named experience) {
        if (experience == null || experience.id() == null) {
            return null;
        }
        return switch (experience.id()) {
            case "noExperience" -> Experience.NO_EXPERIENCE;
            case "between1And3" -> Experience.FROM_1_TO_3;
            case "between3And6" -> Experience.FROM_3_TO_6;
            case "moreThan6" -> Experience.MORE_THAN_6;
            default -> null;
        };
    }

    private static LocalDateTime parseDate(String value) {
        if (value == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value, HH_DATE).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void pause() {
        long ms = props.getRequestDelay().toMillis();
        if (ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
