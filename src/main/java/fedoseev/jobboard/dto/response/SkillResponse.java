package fedoseev.jobboard.dto.response;

import lombok.Getter;
import fedoseev.jobboard.enums.SkillCategory;
import lombok.Setter;

@Getter
@Setter
public class SkillResponse {
     private Long id;

     private String name;

     private SkillCategory category;
}
