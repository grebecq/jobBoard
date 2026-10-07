package fedoseev.jobboard.aggregator;

import fedoseev.jobboard.aggregator.hh.HhClient;
import fedoseev.jobboard.enums.EmploymentType;
import fedoseev.jobboard.enums.Experience;
import fedoseev.jobboard.enums.WorkFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.startsWith;

class HhClientTest {

    private MockRestServiceServer server;
    private HhClient client;

    @BeforeEach
    void setUp() {
        AggregatorProperties props = new AggregatorProperties();
        props.getHh().setRequestDelay(Duration.ZERO);
        props.getHh().setUserAgent("test-agent/1.0 (test@test.local)");
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new HhClient(builder, props);
    }

    @Test
    void searchPageIsMappedToVacancies() {
        server.expect(requestTo(startsWith("https://api.hh.ru/vacancies?")))
                .andExpect(queryParam("text", "java"))
                .andExpect(queryParam("professional_role", "96"))
                .andExpect(queryParam("page", "0"))
                .andExpect(header("HH-User-Agent", "test-agent/1.0 (test@test.local)"))
                .andRespond(withSuccess(new ClassPathResource("hh/search-page.json"), MediaType.APPLICATION_JSON));

        List<ExternalVacancy> result = client.fetchLatest();
        server.verify();

        assertThat(result).hasSize(2);
        ExternalVacancy junior = result.get(0);
        assertThat(junior.externalId()).isEqualTo("101");
        assertThat(junior.url()).isEqualTo("https://hh.ru/vacancy/101");
        assertThat(junior.companyName()).isEqualTo("ООО Ромашка");
        assertThat(junior.companyExternalId()).isEqualTo("555");
        assertThat(junior.city()).isEqualTo("Москва");
        assertThat(junior.salaryFrom()).isEqualTo(120000);
        assertThat(junior.salaryTo()).isEqualTo(180000);
        assertThat(junior.workFormat()).isEqualTo(WorkFormat.REMOTE);
        assertThat(junior.employmentType()).isEqualTo(EmploymentType.FULL_TIME);
        assertThat(junior.experience()).isEqualTo(Experience.NO_EXPERIENCE);
        assertThat(junior.description()).contains("Разработка микросервисов").contains("Знание Java и Spring Boot")
                .doesNotContain("highlighttext");
        assertThat(junior.detailed()).isFalse();
        LocalDateTime expected = OffsetDateTime.parse("2026-10-06T12:30:00+03:00")
                .atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        assertThat(junior.publishedAt()).isEqualTo(expected);

        ExternalVacancy anonymous = result.get(1);
        assertThat(anonymous.salaryFrom()).isNull();
        assertThat(anonymous.companyExternalId()).isNull();
        assertThat(anonymous.workFormat()).isEqualTo(WorkFormat.OFFICE);
        assertThat(anonymous.experience()).isEqualTo(Experience.FROM_3_TO_6);
    }

    @Test
    void detailsBringFullDescriptionAndSkills() {
        server.expect(requestTo("https://api.hh.ru/vacancies/101"))
                .andRespond(withSuccess(new ClassPathResource("hh/vacancy-101.json"), MediaType.APPLICATION_JSON));

        ExternalVacancy full = client.fetchDetails(ExternalVacancy.builder().externalId("101").build());
        server.verify();

        assertThat(full.detailed()).isTrue();
        assertThat(full.description()).isEqualTo("Мы ищем Java-разработчика.\n\n• Spring Boot\n• PostgreSQL & Kafka");
        assertThat(full.skillNames()).containsExactly("Java", "Spring", "Apache Kafka", "Умение слушать");
    }
}
