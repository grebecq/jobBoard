package fedoseev.jobboard.service;

import fedoseev.jobboard.dto.request.SkillRequest;
import fedoseev.jobboard.dto.response.SkillResponse;
import fedoseev.jobboard.entity.Skill;
import fedoseev.jobboard.enums.SkillCategory;
import fedoseev.jobboard.exception.DuplicateResourceException;
import fedoseev.jobboard.mapper.SkillMapper;
import fedoseev.jobboard.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;
    public SkillResponse createSkill(SkillRequest request){

      if (skillRepository.findByName(request.getName()).isPresent()){
          throw new DuplicateResourceException("Skill already exist: " + request.getName());
      }

      Skill skill = skillMapper.toEntity(request);
      if (skill.getCategory() == null) {
          skill.setCategory(SkillCategory.OTHER);
      }
      Skill saved = skillRepository.save(skill);

      return skillMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll(Sort.by("name")).stream()
                .map(skillMapper::toResponse)
                .toList();
    }
}
