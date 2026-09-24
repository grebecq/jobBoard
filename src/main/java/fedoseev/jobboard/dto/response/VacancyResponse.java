package fedoseev.jobboard.dto.response;

import fedoseev.jobboard.enums.VacancyStatus;
import fedoseev.jobboard.enums.EmploymentType;
import fedoseev.jobboard.enums.Experience;
import fedoseev.jobboard.enums.Grade;
import fedoseev.jobboard.enums.Specialization;
import fedoseev.jobboard.enums.WorkFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class VacancyResponse {
    private Long id;

    private String title;

    private String description;

    private Integer salaryFrom;

    private Integer salaryTo;

    private String city;

    private VacancyStatus status;

    private EmploymentType employmentType;

    private Specialization specialization;

    private Grade grade;

    private Experience experience;

    private WorkFormat workFormat;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long companyId;

    private String companyName;

    private Set<String> skillNames;

    // только в кабинете работодателя (/api/vacancies/my), в публичных ответах null
    private Long applicationsCount;

    private Long newApplicationsCount;
}
