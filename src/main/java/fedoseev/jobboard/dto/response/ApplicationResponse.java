package fedoseev.jobboard.dto.response;

import fedoseev.jobboard.entity.Candidate;
import fedoseev.jobboard.enums.ApplicationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cglib.core.Local;

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
}
