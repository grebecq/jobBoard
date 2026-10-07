package fedoseev.jobboard.aggregator;

import fedoseev.jobboard.enums.VacancySource;

import java.util.List;

public interface VacancySourceClient {

    VacancySource source();

    List<ExternalVacancy> fetchLatest();

    default ExternalVacancy fetchDetails(ExternalVacancy vacancy) {
        return vacancy;
    }
}
