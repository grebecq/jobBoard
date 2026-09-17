package fedoseev.jobboard.repository;

import fedoseev.jobboard.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}