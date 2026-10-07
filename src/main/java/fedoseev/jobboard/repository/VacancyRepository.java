package fedoseev.jobboard.repository;
import fedoseev.jobboard.entity.Vacancy;
import fedoseev.jobboard.enums.VacancySource;
import fedoseev.jobboard.enums.VacancyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VacancyRepository extends JpaRepository<Vacancy, Long>, JpaSpecificationExecutor<Vacancy> {

    Page<Vacancy> findByCompany_Owner_Email(String email, Pageable pageable);

    boolean existsBySourceAndExternalId(VacancySource source, String externalId);

    Optional<Vacancy> findBySourceAndExternalId(VacancySource source, String externalId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Vacancy v set v.status = :closed where v.source = :source and v.status = :active and v.lastSeenAt < :seenBefore")
    int closeNotSeenSince(@Param("source") VacancySource source, @Param("seenBefore") LocalDateTime seenBefore,
                          @Param("active") VacancyStatus active, @Param("closed") VacancyStatus closed);
}
