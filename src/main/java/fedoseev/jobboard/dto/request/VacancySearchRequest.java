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

/**
 * Фильтры поиска вакансий. Всё необязательно; списки передаются повтором параметра:
 * {@code ?grade=JUNIOR&grade=MIDDLE}. Внутри одного фильтра — «ИЛИ», между фильтрами — «И».
 */
@Getter
@Setter
@NoArgsConstructor
public class VacancySearchRequest {

    /** Ищется в названии и описании, без учёта регистра. */
    private String q;

    private String city;

    /** Вилка вакансии пересекается с «от N»: salaryTo >= N или salaryFrom >= N. */
    private Integer minSalary;

    private boolean onlyWithSalary;

    private List<Specialization> specialization;

    private List<Grade> grade;

    private List<Experience> experience;

    private List<WorkFormat> workFormat;

    private List<EmploymentType> employmentType;

    /** id навыков; вакансия подходит, если у неё есть хотя бы один из них. */
    private List<Long> skill;

    private Long companyId;

    /** date — сначала новые (по умолчанию), salary — сначала с большей зарплатой. */
    private String order;
}
