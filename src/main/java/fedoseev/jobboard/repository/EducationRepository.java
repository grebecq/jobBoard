package fedoseev.jobboard.repository;

import fedoseev.jobboard.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationRepository extends JpaRepository<Education, Long> {
}
