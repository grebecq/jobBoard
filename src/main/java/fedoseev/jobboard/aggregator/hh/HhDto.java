package fedoseev.jobboard.aggregator.hh;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/** Ответы api.hh.ru, только нужные нам поля. Документация: https://api.hh.ru/openapi/redoc */
final class HhDto {

    private HhDto() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Page(List<Vacancy> items, int found, int pages, int page) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Vacancy(
            String id,
            String name,
            Named area,
            Salary salary,
            @JsonProperty("salary_range") Salary salaryRange,
            Employer employer,
            Snippet snippet,
            @JsonProperty("alternate_url") String alternateUrl,
            @JsonProperty("published_at") String publishedAt,
            Named schedule,
            @JsonProperty("work_format") List<Named> workFormat,
            Named experience,
            Named employment,
            @JsonProperty("employment_form") Named employmentForm,
            String description,
            @JsonProperty("key_skills") List<Named> keySkills,
            boolean archived
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Named(String id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Salary(Integer from, Integer to, String currency, Boolean gross) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Employer(String id, String name, @JsonProperty("logo_urls") Map<String, String> logoUrls) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Snippet(String requirement, String responsibility) {
    }
}
