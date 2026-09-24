package fedoseev.jobboard.dto.request;

import fedoseev.jobboard.enums.EmploymentType;
import fedoseev.jobboard.enums.Experience;
import fedoseev.jobboard.enums.Grade;
import fedoseev.jobboard.enums.Specialization;
import fedoseev.jobboard.enums.WorkFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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

    @PositiveOrZero(message = "Зарплата не может быть отрицательной")
    private Integer salaryFrom;

    @PositiveOrZero(message = "Зарплата не может быть отрицательной")
    private  Integer salaryTo;

    private String city;

    private EmploymentType employmentType;

    private Specialization specialization;

    private Grade grade;

    private Experience experience;

    private WorkFormat workFormat;

    @NotNull(message = "ID company is required")
    private Long companyId;

    @Size(max = 30, message = "Не больше 30 навыков на вакансию")
    private Set<Long> skillIds;

    @AssertTrue(message = "Зарплата «от» не может быть больше зарплаты «до»")
    public boolean isSalaryRangeValid() {
        return salaryFrom == null || salaryTo == null || salaryFrom <= salaryTo;
    }
}
