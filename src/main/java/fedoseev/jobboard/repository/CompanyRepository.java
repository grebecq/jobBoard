package fedoseev.jobboard.repository;

import fedoseev.jobboard.entity.Company;
import fedoseev.jobboard.enums.VacancySource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByOwner_Email(String email);

    Optional<Company> findBySourceAndExternalId(VacancySource source, String externalId);
}
