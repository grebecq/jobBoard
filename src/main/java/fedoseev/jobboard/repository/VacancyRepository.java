package fedoseev.jobboard.repository;
import fedoseev.jobboard.entity.Vacancy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VacancyRepository extends JpaRepository<Vacancy, Long>, JpaSpecificationExecutor<Vacancy> {

    Page<Vacancy> findByCompany_Owner_Email(String email, Pageable pageable);
}
