package fedoseev.jobboard.dto.response;

import fedoseev.jobboard.enums.VacancyStatus;
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

    private String employmentType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long companyId;

    private String companyName;

    private Set<String> skillNames;
}
