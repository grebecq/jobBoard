package fedoseev.jobboard.mapper;

import fedoseev.jobboard.dto.request.SkillRequest;
import fedoseev.jobboard.dto.response.SkillResponse;
import fedoseev.jobboard.entity.Skill;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    Skill toEntity(SkillRequest request);

    SkillResponse toResponse(Skill skill);
}
