package fedoseev.jobboard.dto.response;

import fedoseev.jobboard.enums.ApplicationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationResponse {

    private Long id;

    private ApplicationStatus status;

    private String coverLetter;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long candidateId;

    private Long vacancyId;

    private String vacancyTitle;

    private String vacancyCity;

    private String companyName;

    private String employerComment;

    private String companyContactEmail;

    private String companyTelegram;
}
