package fedoseev.jobboard.aggregator;

import fedoseev.jobboard.entity.Skill;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Сопоставляет навыки из вакансии со справочником навыков jobBoard.
 * Берёт навыки, которые указал источник, и ищет известные технологии в названии и описании.
 */
public final class SkillMatcher {

    // как пишут на сайтах -> как называется в справочнике
    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("spring", "Spring Framework"),
            Map.entry("spring framework", "Spring Framework"),
            Map.entry("spring boot", "Spring Boot"),
            Map.entry("springboot", "Spring Boot"),
            Map.entry("java core", "Java"),
            Map.entry("core java", "Java"),
            Map.entry("java se", "Java"),
            Map.entry("java ee", "Jakarta EE"),
            Map.entry("j2ee", "Jakarta EE"),
            Map.entry("postgres", "PostgreSQL"),
            Map.entry("postgresql", "PostgreSQL"),
            Map.entry("ms sql", "MS SQL Server"),
            Map.entry("mssql", "MS SQL Server"),
            Map.entry("k8s", "Kubernetes"),
            Map.entry("rest", "REST API"),
            Map.entry("restful api", "REST API"),
            Map.entry("микросервисы", "Microservices"),
            Map.entry("микросервисная архитектура", "Microservices"),
            Map.entry("ci/cd", "CI/CD"),
            Map.entry("oop", "ООП"),
            Map.entry("ооп", "ООП"),
            Map.entry("multithreading", "Многопоточность"),
            Map.entry("design patterns", "Паттерны проектирования"),
            Map.entry("gitlab", "GitLab CI"),
            Map.entry("elk stack", "ELK")
    );

    // слишком короткие или слишком общие слова, чтобы искать их в тексте описания
    private static final Set<String> NOT_IN_TEXT = Set.of("c", "r", "go", "rest api", "express", "gin", "vault", "unity", "spark", "helm");

    private SkillMatcher() {
    }

    public static Set<Skill> match(List<Skill> dictionary, Collection<String> declared, String title, String description) {
        Map<String, Skill> byName = new HashMap<>();
        for (Skill skill : dictionary) {
            byName.put(normalize(skill.getName()), skill);
        }

        Set<Skill> result = new HashSet<>();
        if (declared != null) {
            for (String name : declared) {
                Skill skill = lookup(name, byName);
                if (skill != null) {
                    result.add(skill);
                }
            }
        }

        String text = ((title == null ? "" : title) + "\n" + (description == null ? "" : description)).toLowerCase(Locale.ROOT);
        for (Skill skill : dictionary) {
            String name = normalize(skill.getName());
            if (!NOT_IN_TEXT.contains(name) && containsWord(text, name)) {
                result.add(skill);
            }
        }
        return result;
    }

    private static Skill lookup(String raw, Map<String, Skill> byName) {
        String name = normalize(raw);
        if (name.startsWith("apache ")) {
            name = name.substring("apache ".length());
        }
        Skill skill = byName.get(name);
        if (skill == null && ALIASES.containsKey(name)) {
            skill = byName.get(normalize(ALIASES.get(name)));
        }
        return skill;
    }

    static boolean containsWord(String text, String word) {
        // C++, C#, .NET: обычный \b тут не работает, поэтому границы проверяем сами
        Pattern p = Pattern.compile("(?<![\\p{L}\\p{N}+#.])" + Pattern.quote(word) + "(?![\\p{L}\\p{N}+#])");
        return p.matcher(text).find();
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
}
