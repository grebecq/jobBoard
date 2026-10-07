package fedoseev.jobboard.aggregator;

import fedoseev.jobboard.enums.VacancySource;

import java.util.List;

/** Один сайт с вакансиями. Чтобы добавить новый сайт, достаточно ещё одной реализации-бина. */
public interface VacancySourceClient {

    VacancySource source();

    /** Свежие вакансии из выдачи поиска. Описание может быть кратким (detailed = false). */
    List<ExternalVacancy> fetchLatest();

    /** Полная карточка вакансии: описание и навыки. Вызывается только для новых вакансий. */
    default ExternalVacancy fetchDetails(ExternalVacancy vacancy) {
        return vacancy;
    }
}
