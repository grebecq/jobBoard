package fedoseev.jobboard.dto.response;

import fedoseev.jobboard.enums.ApplicationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EmployerApplicationResponse {

    private Long id;

    private ApplicationStatus status;

    private String coverLetter;

    private String employerComment;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long vacancyId;

    private String vacancyTitle;

    private Long candidateId;

    private String candidateFirstName;

    private String candidateLastName;

    private String candidateEmail;

    private String candidatePhone;

    private String candidateTelegram;

    private String candidateCity;
}
