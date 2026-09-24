package fedoseev.jobboard.repository;

/**
 * Проекция для счётчиков откликов в кабинете работодателя.
 */
public interface VacancyApplicationStats {

    Long getVacancyId();

    Long getTotal();

    Long getFresh();
}
