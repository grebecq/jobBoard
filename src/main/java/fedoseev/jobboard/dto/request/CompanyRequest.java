package fedoseev.jobboard.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class CompanyRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    private String description;

    private String logoUrl;

    private String website;

    @Email(message = "Некорректный email для связи")
    @Size(max = 255)
    private String contactEmail;

    // ник, @ник или ссылка t.me — нормализуется в сервисе
    @Size(max = 64)
    private String telegram;



}
