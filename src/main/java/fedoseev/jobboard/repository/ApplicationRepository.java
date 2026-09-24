package fedoseev.jobboard.repository;

import fedoseev.jobboard.entity.Application;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application,Long> {
    boolean existsByCandidateIdAndVacancyId(Long candidateId, Long vacancyId);

    @EntityGraph(attributePaths = {"vacancy", "vacancy.company", "vacancy.company.owner"})
    List<Application> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);

    @EntityGraph(attributePaths = {"candidate", "vacancy"})
    List<Application> findByVacancyIdOrderByCreatedAtDesc(Long vacancyId);

    @EntityGraph(attributePaths = {"candidate", "vacancy", "vacancy.company", "vacancy.company.owner"})
    Optional<Application> findWithDetailsById(Long id);

    @Query("""
            select a.vacancy.id as vacancyId,
                   count(a) as total,
                   sum(case when a.status = fedoseev.jobboard.enums.ApplicationStatus.PENDING then 1 else 0 end) as fresh
            from Application a
            where a.vacancy.id in :vacancyIds
            group by a.vacancy.id
            """)
    List<VacancyApplicationStats> statsByVacancyIds(@Param("vacancyIds") Collection<Long> vacancyIds);
}
