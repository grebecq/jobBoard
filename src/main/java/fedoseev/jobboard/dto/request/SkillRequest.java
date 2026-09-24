package fedoseev.jobboard.dto.request;

import jakarta.validation.constraints.NotBlank;
import fedoseev.jobboard.enums.SkillCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SkillRequest {

    @NotBlank(message = "Skill name is required")
    private String name;

    private SkillCategory category;
}
