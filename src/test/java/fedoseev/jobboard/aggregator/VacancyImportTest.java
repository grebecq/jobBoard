package fedoseev.jobboard.aggregator;

import fedoseev.jobboard.dto.request.RegisterRequest;
import fedoseev.jobboard.entity.Skill;
import fedoseev.jobboard.entity.Vacancy;
import fedoseev.jobboard.enums.EmploymentType;
import fedoseev.jobboard.enums.Experience;
import fedoseev.jobboard.enums.Grade;
import fedoseev.jobboard.enums.Role;
import fedoseev.jobboard.enums.VacancySource;
import fedoseev.jobboard.enums.VacancyStatus;
import fedoseev.jobboard.enums.WorkFormat;
import fedoseev.jobboard.repository.VacancyRepository;
import fedoseev.jobboard.service.AuthService;
import fedoseev.jobboard.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VacancyImportTest {

    @Autowired
    private VacancyImportService importService;
    @Autowired
    private VacancyRepository vacancyRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtService jwtService;

    /** Источник-заглушка вместо настоящего hh.ru. */
    static class FakeSource implements VacancySourceClient {
        List<ExternalVacancy> latest = new ArrayList<>();
        int detailsCalls;

        @Override
        public VacancySource source() {
            return VacancySource.HH;
        }

        @Override
        public List<ExternalVacancy> fetchLatest() {
            return latest;
        }

        @Override
        public ExternalVacancy fetchDetails(ExternalVacancy v) {
            detailsCalls++;
            return v.toBuilder()
                    .description("Пишем на Java 21 и Spring Boot, база PostgreSQL.")
                    .skillNames(List.of("Apache Kafka", "Spring", "Умение работать в команде"))
                    .detailed(true)
                    .build();
        }
    }

    private static ExternalVacancy hh(String id, String title) {
        return ExternalVacancy.builder()
                .externalId(id)
                .url("https://hh.ru/vacancy/" + id)
                .title(title)
                .description("кратко")
                .companyExternalId("company-" + id.charAt(0))
                .companyName("Компания " + id.charAt(0))
                .city("Москва")
                .salaryFrom(150000)
                .workFormat(WorkFormat.REMOTE)
                .employmentType(EmploymentType.FULL_TIME)
                .experience(Experience.NO_EXPERIENCE)
                .publishedAt(LocalDateTime.of(2026, 10, 1, 10, 0))
                .skillNames(List.of())
                .build();
    }

    private Vacancy saved(String externalId) {
        return vacancyRepository.findBySourceAndExternalId(VacancySource.HH, externalId).orElseThrow();
    }

    @Test
    void importCreatesVacancyWithCompanySkillsAndGrade() {
        String id = "1" + UUID.randomUUID();
        FakeSource source = new FakeSource();
        source.latest.add(hh(id, "Java-разработчик"));

        ImportResult result = importService.importFrom(source);

        assertThat(result.created()).isEqualTo(1);
        assertThat(source.detailsCalls).isEqualTo(1);
        Vacancy v = saved(id);
        assertThat(v.getSource()).isEqualTo(VacancySource.HH);
        assertThat(v.getExternalUrl()).isEqualTo("https://hh.ru/vacancy/" + id);
        assertThat(v.getCompany().getName()).isEqualTo("Компания 1");
        assertThat(v.getCompany().getOwner()).isNull();
        assertThat(v.getGrade()).isEqualTo(Grade.JUNIOR);
        assertThat(v.getPublishedAt()).isEqualTo(LocalDateTime.of(2026, 10, 1, 10, 0));
        assertThat(v.getSkills()).extracting(Skill::getName)
                .contains("Java", "Spring Boot", "Spring Framework", "PostgreSQL", "Kafka")
                .doesNotContain("JavaScript");
    }

    @Test
    void secondRunUpdatesInsteadOfDuplicating() {
        String id = "2" + UUID.randomUUID();
        FakeSource source = new FakeSource();
        source.latest.add(hh(id, "Java-разработчик"));
        importService.importFrom(source);

        source.latest.set(0, hh(id, "Java-разработчик").toBuilder().salaryFrom(200000).build());
        ImportResult second = importService.importFrom(source);

        assertThat(second.created()).isZero();
        assertThat(second.updated()).isEqualTo(1);
        assertThat(source.detailsCalls).isEqualTo(1);
        assertThat(saved(id).getSalaryFrom()).isEqualTo(200000);
    }

    @Test
    void vacanciesMissingFromSourceForLongAreClosed() {
        String id = "3" + UUID.randomUUID();
        FakeSource source = new FakeSource();
        source.latest.add(hh(id, "Java-разработчик"));
        importService.importFrom(source);
        saved(id).setLastSeenAt(LocalDateTime.now().minusDays(30));
        vacancyRepository.flush();

        source.latest.clear();
        ImportResult result = importService.importFrom(source);

        assertThat(result.closed()).isGreaterThanOrEqualTo(1);
        vacancyRepository.flush();
        assertThat(vacancyRepository.findBySourceAndExternalId(VacancySource.HH, id))
                .get().extracting(Vacancy::getStatus).isEqualTo(VacancyStatus.CLOSED);
    }

    @Test
    void importedVacancyIsSearchableBySourceButNotApplicable() throws Exception {
        String id = "4" + UUID.randomUUID();
        FakeSource source = new FakeSource();
        source.latest.add(hh(id, "Junior Java " + id));
        importService.importFrom(source);
        long vacancyId = saved(id).getId();

        mockMvc.perform(get("/api/vacancies/search").param("source", "HH").param("q", id))
                .andExpect(jsonPath("$.content[*].id", contains((int) vacancyId)))
                .andExpect(jsonPath("$.content[0].source").value("HH"))
                .andExpect(jsonPath("$.content[0].externalUrl").value("https://hh.ru/vacancy/" + id));

        mockMvc.perform(get("/api/vacancies/search").param("source", "JOBBOARD").param("q", id))
                .andExpect(jsonPath("$.page.totalElements").value(0));

        mockMvc.perform(get("/api/dictionaries"))
                .andExpect(jsonPath("$.sources[*].value", hasItem("HH")));

        String email = "agg_" + UUID.randomUUID() + "@test.local";
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("secret123");
        request.setRole(Role.CANDIDATE);
        authService.register(request);
        mockMvc.perform(post("/api/applications/apply/" + vacancyId)
                        .header("Authorization", "Bearer " + jwtService.generateToken(email)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void manualRunIsAdminOnly() throws Exception {
        String email = "agg_" + UUID.randomUUID() + "@test.local";
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("secret123");
        request.setRole(Role.EMPLOYER);
        authService.register(request);
        mockMvc.perform(post("/api/admin/aggregator/run")
                        .header("Authorization", "Bearer " + jwtService.generateToken(email)))
                .andExpect(status().isForbidden());
    }
}
