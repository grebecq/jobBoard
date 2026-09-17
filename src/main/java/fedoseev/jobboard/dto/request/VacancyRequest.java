package fedoseev.jobboard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor

public class VacancyRequest {

    @NotBlank(message = "Chose a job title")
    private String title;

    @NotBlank(message = "The description is required ")
    private String description;

    private Integer salaryFrom;

    private  Integer salaryTo;

    private String city;

    private String employmentType;
    @NotNull(message = "ID company is required")
    private Long companyId;

    private Set<Long> skillIds;

}
