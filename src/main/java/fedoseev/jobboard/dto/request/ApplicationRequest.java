package fedoseev.jobboard.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationRequest {

    @NotNull
    private Long candidateId;

    @NotNull
    private Long vacancyId;

    private String  coverLetter;
}
