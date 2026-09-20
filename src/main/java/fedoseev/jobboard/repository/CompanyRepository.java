package fedoseev.jobboard.repository;

import fedoseev.jobboard.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByOwner_Email(String email);
}
