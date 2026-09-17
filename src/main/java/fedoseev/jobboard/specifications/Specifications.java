package fedoseev.jobboard.specifications;

import fedoseev.jobboard.entity.Vacancy;
import org.springframework.data.jpa.domain.Specification;

public class Specifications {

    public static Specification<Vacancy> hasCity(String city) {
        if (city == null || city.isBlank()) {
            return Specification.unrestricted(); // в 4 спринге нельзя отдавать null т.к. код упадет
        }
        return (root, query, cb) -> cb.equal(root.get("city"), city);
    }

    public static Specification<Vacancy> salaryFromAtLeast(Integer minSalary) {
        if(minSalary == null){
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("salaryFrom"), minSalary);
    }

    public static Specification<Vacancy> hasEmploymentType(String type) {
        if(type == null || type.isBlank()){
            return  Specification.unrestricted();
        }
        return(root, query, cb) -> cb.equal(root.get("employmentType"), type);
    }

}
