package fedoseev.jobboard.dto.response;

import fedoseev.jobboard.enums.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterResponse {
    private Long id;

    private String email;

    private Role role;
}
