package fedoseev.jobboard.repository;

import fedoseev.jobboard.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillRepository  extends JpaRepository<Skill, Long> {
    Optional<Skill> findByName(String name);
}
