package fedoseev.jobboard.aggregator;

import fedoseev.jobboard.enums.Experience;
import fedoseev.jobboard.enums.Grade;
import fedoseev.jobboard.enums.Specialization;

import java.util.Locale;
import java.util.regex.Pattern;

public final class VacancyClassifier {

    private static final Pattern INTERN = word("стажер|стажёр|intern|internship|trainee|практикант");
    private static final Pattern JUNIOR = word("junior|джуниор|младший");
    private static final Pattern MIDDLE = word("middle|мидл");
    private static final Pattern SENIOR = word("senior|сеньор|синьор|старший|ведущий");
    private static final Pattern LEAD = word("lead|teamlead|techlead|лид|тимлид|техлид|руководитель|head");

    private VacancyClassifier() {
    }

    public static Grade grade(String title, Experience experience) {
        String t = lower(title);
        if (LEAD.matcher(t).find()) return Grade.LEAD;
        if (INTERN.matcher(t).find()) return Grade.INTERN;
        if (JUNIOR.matcher(t).find()) return Grade.JUNIOR;
        if (MIDDLE.matcher(t).find()) return Grade.MIDDLE;
        if (SENIOR.matcher(t).find()) return Grade.SENIOR;
        if (experience == null) return null;
        return switch (experience) {
            case NO_EXPERIENCE -> Grade.JUNIOR;
            case FROM_1_TO_3 -> Grade.MIDDLE;
            case FROM_3_TO_6, MORE_THAN_6 -> Grade.SENIOR;
        };
    }

    public static Specialization specialization(String title) {
        String t = lower(title);
        if (t.contains("fullstack") || t.contains("full stack") || t.contains("full-stack")) return Specialization.FULLSTACK;
        if (t.contains("android") || t.contains("ios") || t.contains("mobile") || t.contains("мобильн")) return Specialization.MOBILE;
        if (t.contains("qa") || t.contains("тестиров") || t.contains("test")) return Specialization.QA;
        if (t.contains("devops") || t.contains("sre")) return Specialization.DEVOPS;
        if (t.contains("data engineer")) return Specialization.DATA_ENGINEER;
        if (t.contains("архитект") || t.contains("architect")) return Specialization.ARCHITECT;
        if (t.contains("аналитик") || t.contains("analyst")) return Specialization.SYSTEM_ANALYST;
        return Specialization.BACKEND;
    }

    private static String lower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    private static Pattern word(String alternatives) {
        return Pattern.compile("(?<![\\p{L}\\p{N}])(" + alternatives + ")(?![\\p{L}\\p{N}])");
    }
}
