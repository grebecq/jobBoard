package fedoseev.jobboard.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class CompanyResponse {

    private Long id;

    private String name;

    private String description;

    private String logoUrl;

    private String website;

    private LocalDateTime createdAt;

}
