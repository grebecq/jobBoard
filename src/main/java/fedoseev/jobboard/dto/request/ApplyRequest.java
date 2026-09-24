package fedoseev.jobboard.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApplyRequest {

    @Size(max = 5000, message = "Сопроводительное письмо — не больше 5000 символов")
    private String coverLetter;
}
