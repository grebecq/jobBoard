package fedoseev.jobboard.aggregator;

import fedoseev.jobboard.enums.VacancySource;

/** Итог сбора с одного источника. */
public record ImportResult(VacancySource source, int fetched, int created, int updated, int closed, String error) {

    public static ImportResult failed(VacancySource source, String error) {
        return new ImportResult(source, 0, 0, 0, 0, error);
    }
}
