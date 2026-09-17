package fedoseev.jobboard.service;

import fedoseev.jobboard.dto.request.SkillRequest;
import fedoseev.jobboard.dto.response.SkillResponse;
import fedoseev.jobboard.entity.Skill;
import fedoseev.jobboard.exception.DuplicateResourceException;
import fedoseev.jobboard.mapper.SkillMapper;
import fedoseev.jobboard.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
      Skill saved = skillRepository.save(skill);

      return skillMapper.toResponse(saved);
    }
}
