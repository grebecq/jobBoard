package fedoseev.jobboard.repository;

import fedoseev.jobboard.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
}
