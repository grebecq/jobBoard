package fedoseev.jobboard.dto.request;

import fedoseev.jobboard.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Укажите email")
    @Email(message = "Email указан неверно")
    private String email;

    @NotBlank(message = "Укажите пароль")
    @Size(min = 6, max = 100, message = "Пароль должен быть от 6 до 100 символов")
    private String password;

    @NotNull(message = "Выберите роль")
    private Role role;

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }
}
