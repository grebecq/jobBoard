package fedoseev.jobboard.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CandidateResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String city;

    private LocalDateTime createdAt;
}
