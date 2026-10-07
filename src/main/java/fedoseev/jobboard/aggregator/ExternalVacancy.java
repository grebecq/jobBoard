package fedoseev.jobboard.aggregator;

import fedoseev.jobboard.enums.EmploymentType;
import fedoseev.jobboard.enums.Experience;
import fedoseev.jobboard.enums.WorkFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Вакансия с внешнего сайта в общем виде, до сохранения в базу.
 * description уже очищен от HTML, skillNames - как их написал источник.
 */
@Builder(toBuilder = true)
public record ExternalVacancy(
        String externalId,
        String url,
        String title,
        String description,
        String companyExternalId,
        String companyName,
        String companyLogoUrl,
        String city,
        Integer salaryFrom,
        Integer salaryTo,
        WorkFormat workFormat,
        EmploymentType employmentType,
        Experience experience,
        List<String> skillNames,
        LocalDateTime publishedAt,
        boolean detailed
) {
}
