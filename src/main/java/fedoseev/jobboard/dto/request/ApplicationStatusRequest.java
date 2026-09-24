package fedoseev.jobboard.dto.request;

import fedoseev.jobboard.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationStatusRequest {

    @NotNull(message = "Укажите статус")
    private ApplicationStatus status;

    @Size(max = 2000, message = "Комментарий — не больше 2000 символов")
    private String comment;
}
