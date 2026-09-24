package fedoseev.jobboard;

import com.jayway.jsonpath.JsonPath;
import fedoseev.jobboard.dto.request.RegisterRequest;
import fedoseev.jobboard.entity.Vacancy;
import fedoseev.jobboard.enums.Role;
import fedoseev.jobboard.enums.VacancyStatus;
import fedoseev.jobboard.repository.SkillRepository;
import fedoseev.jobboard.repository.VacancyRepository;
import fedoseev.jobboard.service.AuthService;
import fedoseev.jobboard.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Поиск по IT-критериям. Все запросы ограничены своей компанией (companyId),
 * чтобы не зависеть от вакансий, которые уже лежат в dev-базе.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VacancySearchTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private SkillRepository skillRepository;
    @Autowired
    private VacancyRepository vacancyRepository;

    private String employer;
    private long companyId;

    @BeforeEach
    void setUp() throws Exception {
        String email = "search_" + UUID.randomUUID() + "@test.local";
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("secret123");
        request.setRole(Role.EMPLOYER);
        authService.register(request);
        employer = "Bearer " + jwtService.generateToken(email);

        companyId = id(mockMvc.perform(post("/api/companies")
                        .header("Authorization", employer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Search Co\"}"))
                .andReturn().getResponse().getContentAsString());
    }

    private long id(String json) {
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    private long skill(String name) {
        return skillRepository.findByName(name).orElseThrow().getId();
    }

    private long vacancy(String json) throws Exception {
        String body = json.replace("}", ",\"description\":\"desc\",\"companyId\":" + companyId + "}");
        return id(mockMvc.perform(post("/api/vacancies")
                        .header("Authorization", employer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private MockHttpServletRequestBuilder search() {
        return get("/api/vacancies/search").param("companyId", String.valueOf(companyId));
    }

    @Test
    void dictionaries_arePublicAndContainItSkills() throws Exception {
        mockMvc.perform(get("/api/dictionaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grades[*].value", contains("INTERN", "JUNIOR", "MIDDLE", "SENIOR", "LEAD")))
                .andExpect(jsonPath("$.specializations[?(@.value == 'BACKEND')].label", contains("Бэкенд-разработка")))
                .andExpect(jsonPath("$.skills[?(@.name == 'Kotlin')].category", contains("LANGUAGE")))
                .andExpect(jsonPath("$.skills[?(@.name == 'Kubernetes')].category", contains("DEVOPS")))
                .andExpect(jsonPath("$.skills.length()", greaterThan(150)));
    }

    @Test
    void filtersByGradeSpecializationAndFormat() throws Exception {
        vacancy("{\"title\":\"Java Junior\",\"specialization\":\"BACKEND\",\"grade\":\"JUNIOR\",\"workFormat\":\"REMOTE\"}");
        vacancy("{\"title\":\"Java Middle\",\"specialization\":\"BACKEND\",\"grade\":\"MIDDLE\",\"workFormat\":\"OFFICE\"}");
        vacancy("{\"title\":\"React Senior\",\"specialization\":\"FRONTEND\",\"grade\":\"SENIOR\",\"workFormat\":\"REMOTE\"}");

        mockMvc.perform(search().param("grade", "JUNIOR").param("grade", "MIDDLE"))
                .andExpect(jsonPath("$.content[*].title", containsInAnyOrder("Java Junior", "Java Middle")));

        mockMvc.perform(search().param("specialization", "BACKEND").param("workFormat", "REMOTE"))
                .andExpect(jsonPath("$.content[*].title", contains("Java Junior")))
                .andExpect(jsonPath("$.content[0].grade").value("JUNIOR"));
    }

    @Test
    void skillFilter_matchesAnyWithoutDuplicates() throws Exception {
        long kotlin = skill("Kotlin");
        long spring = skill("Spring Boot");
        long react = skill("React");
        vacancy("{\"title\":\"Both\",\"skillIds\":[" + kotlin + "," + spring + "]}");
        vacancy("{\"title\":\"Kotlin only\",\"skillIds\":[" + kotlin + "]}");
        vacancy("{\"title\":\"Frontend\",\"skillIds\":[" + react + "]}");

        mockMvc.perform(search().param("skill", String.valueOf(kotlin)).param("skill", String.valueOf(spring)))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].title", containsInAnyOrder("Both", "Kotlin only")));
    }

    @Test
    void textAndCitySearch_ignoreCase() throws Exception {
        vacancy("{\"title\":\"Senior KOTLIN developer\",\"city\":\"Москва\"}");
        vacancy("{\"title\":\"Python dev\",\"city\":\"Казань\"}");

        mockMvc.perform(search().param("q", "kotlin"))
                .andExpect(jsonPath("$.content[*].title", contains("Senior KOTLIN developer")));
        // текст ищется и по названиям навыков
        vacancy("{\"title\":\"Backend dev\",\"skillIds\":[" + skill("Kotlin") + "]}");
        mockMvc.perform(search().param("q", "KOTLIN"))
                .andExpect(jsonPath("$.content[*].title", containsInAnyOrder("Senior KOTLIN developer", "Backend dev")));
        mockMvc.perform(search().param("city", "москва"))
                .andExpect(jsonPath("$.content[*].title", contains("Senior KOTLIN developer")));
        // спецсимволы LIKE ищутся буквально
        mockMvc.perform(search().param("q", "%"))
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }

    @Test
    void minSalary_likeHh() throws Exception {
        vacancy("{\"title\":\"Range up to 300\",\"salaryFrom\":150000,\"salaryTo\":300000}");
        vacancy("{\"title\":\"Low\",\"salaryFrom\":50000,\"salaryTo\":80000}");
        vacancy("{\"title\":\"From 250\",\"salaryFrom\":250000}");
        vacancy("{\"title\":\"No salary\"}");

        mockMvc.perform(search().param("minSalary", "200000"))
                .andExpect(jsonPath("$.content[*].title", containsInAnyOrder("Range up to 300", "From 250", "No salary")));

        mockMvc.perform(search().param("minSalary", "200000").param("onlyWithSalary", "true"))
                .andExpect(jsonPath("$.content[*].title", containsInAnyOrder("Range up to 300", "From 250")));
    }

    @Test
    void orderBySalary_highestFirst_unspecifiedLast() throws Exception {
        vacancy("{\"title\":\"No salary\"}");
        vacancy("{\"title\":\"Up to 300\",\"salaryFrom\":100000,\"salaryTo\":300000}");
        vacancy("{\"title\":\"From 250\",\"salaryFrom\":250000}");
        vacancy("{\"title\":\"Up to 80\",\"salaryTo\":80000}");

        mockMvc.perform(search().param("order", "salary").param("size", "2"))
                .andExpect(jsonPath("$.page.totalElements").value(4))
                .andExpect(jsonPath("$.content[*].title", contains("Up to 300", "From 250")));
        mockMvc.perform(search().param("order", "salary").param("size", "2").param("page", "1"))
                .andExpect(jsonPath("$.content[*].title", contains("Up to 80", "No salary")));
    }

    @Test
    void closedVacancies_hidden_andNewestFirst() throws Exception {
        vacancy("{\"title\":\"Old\"}");
        long closed = vacancy("{\"title\":\"Closed\"}");
        vacancy("{\"title\":\"New\"}");

        Vacancy v = vacancyRepository.findById(closed).orElseThrow();
        v.setStatus(VacancyStatus.CLOSED);
        vacancyRepository.flush();

        mockMvc.perform(search())
                .andExpect(jsonPath("$.content[*].title", contains("New", "Old")));
    }

    @Test
    void createVacancy_validatesSalaryRangeAndSkills() throws Exception {
        String base = ",\"description\":\"d\",\"companyId\":" + companyId + "}";
        mockMvc.perform(post("/api/vacancies").header("Authorization", employer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Bad\",\"salaryFrom\":300000,\"salaryTo\":100000" + base))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/vacancies").header("Authorization", employer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Bad\",\"skillIds\":[99999999]" + base))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/vacancies").header("Authorization", employer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Bad\",\"grade\":\"GURU\"" + base))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownFilterValue_returns400() throws Exception {
        mockMvc.perform(search().param("grade", "GURU"))
                .andExpect(status().isBadRequest());
    }
}
