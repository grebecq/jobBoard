package fedoseev.jobboard.aggregator;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "aggregator")
public class AggregatorProperties {

    /** Включает сбор по расписанию. */
    private boolean enabled = false;

    /** Запустить сбор сразу после старта приложения. */
    private boolean runOnStartup = false;

    /** Через сколько дней без появления в выдаче вакансия считается закрытой. */
    private int staleDays = 14;

    /** Сколько полных карточек новых вакансий загружать с одного источника за запуск. */
    private int maxDetailsPerRun = 300;

    private Hh hh = new Hh();

    @Getter
    @Setter
    public static class Hh {

        private boolean enabled = true;

        private String baseUrl = "https://api.hh.ru";

        /** hh.ru требует заголовок HH-User-Agent в формате "Название/версия (почта)". */
        private String userAgent = "jobBoard-aggregator/1.0 (jobboard@example.com)";

        /** Токен приложения с dev.hh.ru, если анонимный доступ ограничат. */
        private String token;

        private String text = "java";

        /** 96 - «Программист, разработчик». */
        private List<String> professionalRoles = List.of("96");

        /** Регионы hh.ru, пусто - вся выдача. 113 - Россия. */
        private List<String> areas = List.of();

        /** За сколько дней брать вакансии. */
        private int periodDays = 30;

        /** hh.ru отдаёт не больше 2000 вакансий на один запрос (20 страниц по 100). */
        private int maxPages = 20;

        private int perPage = 100;

        /** Пауза между запросами, чтобы не нагружать API. */
        private Duration requestDelay = Duration.ofMillis(250);
    }
}
