package fedoseev.jobboard.specifications;

import fedoseev.jobboard.entity.Skill;
import fedoseev.jobboard.entity.Vacancy;
import fedoseev.jobboard.enums.VacancyStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Nulls;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.Locale;

public class Specifications {

    public static Specification<Vacancy> hasCity(String city) {
        if (city == null || city.isBlank()) {
            return Specification.unrestricted(); // в 4 спринге нельзя отдавать null т.к. код упадет
        }
        String value = city.trim().toLowerCase(Locale.ROOT);
        return (root, query, cb) -> cb.equal(cb.lower(root.<String>get("city")), value);
    }

    /**
     * Как на hh.ru: «доход от N» — вилка вакансии дотягивает до N.
     * Вакансии без зарплаты остаются в выдаче, если не включено «только с зарплатой».
     */
    public static Specification<Vacancy> salaryAtLeast(Integer minSalary, boolean includeUnspecified) {
        if (minSalary == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> {
            var from = root.<Integer>get("salaryFrom");
            var to = root.<Integer>get("salaryTo");
            var matches = cb.or(
                    cb.greaterThanOrEqualTo(to, minSalary),
                    cb.and(cb.isNull(to), cb.greaterThanOrEqualTo(from, minSalary)));
            return includeUnspecified
                    ? cb.or(matches, cb.and(cb.isNull(from), cb.isNull(to)))
                    : matches;
        };
    }

    public static Specification<Vacancy> withSalaryOnly(boolean onlyWithSalary) {
        if (!onlyWithSalary) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.or(cb.isNotNull(root.get("salaryFrom")), cb.isNotNull(root.get("salaryTo")));
    }

    public static Specification<Vacancy> textContains(String text) {
        if (text == null || text.isBlank()) {
            return Specification.unrestricted();
        }
        String pattern = "%" + text.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        return (root, query, cb) -> {
            // «kotlin» находит и вакансию, где Kotlin указан только в стеке
            Subquery<Long> bySkill = query.subquery(Long.class);
            Root<Vacancy> v = bySkill.from(Vacancy.class);
            Join<Vacancy, Skill> s = v.join("skills");
            bySkill.select(v.get("id")).where(
                    cb.equal(v.get("id"), root.get("id")),
                    cb.like(cb.lower(s.<String>get("name")), pattern, '\\'));

            return cb.or(
                    cb.like(cb.lower(root.<String>get("title")), pattern, '\\'),
                    cb.like(cb.lower(root.<String>get("description")), pattern, '\\'),
                    cb.exists(bySkill));
        };
    }

    /** Поле входит в список значений; пустой список = фильтр не задан. */
    public static Specification<Vacancy> fieldIn(String field, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> root.get(field).in(values);
    }

    public static Specification<Vacancy> isActive() {
        return (root, query, cb) -> cb.equal(root.get("status"), VacancyStatus.ACTIVE);
    }

    public static Specification<Vacancy> ofCompany(Long companyId) {
        if (companyId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("company").get("id"), companyId);
    }

    /**
     * Сортировка «сначала дороже»: по верхней границе вилки (или нижней, если верхней нет),
     * вакансии без зарплаты — в конце. Обычным ?sort= так не сделать: в Postgres NULL при DESC идут первыми.
     */
    public static Specification<Vacancy> orderBySalaryDesc() {
        return (root, query, cb) -> {
            // для count-запроса пагинации сортировка не нужна и в Postgres ломает агрегат
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.orderBy(
                        cb.desc(cb.coalesce(root.<Integer>get("salaryTo"), root.<Integer>get("salaryFrom")), Nulls.LAST),
                        cb.desc(root.get("createdAt")),
                        cb.desc(root.get("id")));
            }
            return null;
        };
    }

    /**
     * Есть хотя бы один из навыков. Через подзапрос EXISTS, а не join —
     * иначе вакансия с двумя подходящими навыками задвоится и сломает пагинацию.
     */
    public static Specification<Vacancy> hasAnySkill(Collection<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<Vacancy> v = sub.from(Vacancy.class);
            Join<Vacancy, Skill> s = v.join("skills");
            sub.select(v.get("id"))
                    .where(cb.equal(v.get("id"), root.get("id")), s.get("id").in(skillIds));
            return cb.exists(sub);
        };
    }
}
