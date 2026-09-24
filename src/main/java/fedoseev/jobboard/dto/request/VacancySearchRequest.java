package fedoseev.jobboard.dto.request;

import fedoseev.jobboard.enums.EmploymentType;
import fedoseev.jobboard.enums.Experience;
import fedoseev.jobboard.enums.Grade;
import fedoseev.jobboard.enums.Specialization;
import fedoseev.jobboard.enums.WorkFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class VacancySearchRequest {

    private String q;

    private String city;

    private Integer minSalary;

    private boolean onlyWithSalary;

    private List<Specialization> specialization;

    private List<Grade> grade;

    private List<Experience> experience;

    private List<WorkFormat> workFormat;

    private List<EmploymentType> employmentType;

    private List<Long> skill;

    private Long companyId;

    private String order;
}
