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

    private boolean enabled = false;

    private boolean runOnStartup = false;

    private int staleDays = 14;

    private int maxDetailsPerRun = 300;

    private Hh hh = new Hh();

    @Getter
    @Setter
    public static class Hh {

        private boolean enabled = true;

        private String baseUrl = "https://api.hh.ru";

        private String userAgent = "jobBoard-aggregator/1.0 (jobboard@example.com)";

        private String token;

        private String text = "java";

        private List<String> professionalRoles = List.of("96");

        private List<String> areas = List.of();

        private int periodDays = 30;

        private int maxPages = 20;

        private int perPage = 100;

        private Duration requestDelay = Duration.ofMillis(250);
    }
}
