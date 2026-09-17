package fedoseev.jobboard.repository;

import fedoseev.jobboard.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application,Long> {
    boolean existsByCandidateIdAndVacancyId(Long candidateId, Long vacancyId);

}
