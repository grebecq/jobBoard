package fedoseev.jobboard.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Укажите email")
    @Email(message = "Email указан неверно")
    private String email;

    @NotBlank(message = "Укажите пароль")
    private String password;

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }
}
