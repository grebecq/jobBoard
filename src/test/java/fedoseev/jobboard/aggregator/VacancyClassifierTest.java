package fedoseev.jobboard.aggregator;

import fedoseev.jobboard.enums.Experience;
import fedoseev.jobboard.enums.Grade;
import fedoseev.jobboard.enums.Specialization;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VacancyClassifierTest {

    @Test
    void gradeFromTitleWinsOverExperience() {
        assertThat(VacancyClassifier.grade("Junior Java developer", Experience.FROM_3_TO_6)).isEqualTo(Grade.JUNIOR);
        assertThat(VacancyClassifier.grade("Старший Java-разработчик", null)).isEqualTo(Grade.SENIOR);
        assertThat(VacancyClassifier.grade("Senior/Lead Java", null)).isEqualTo(Grade.LEAD);
        assertThat(VacancyClassifier.grade("Стажёр Java", null)).isEqualTo(Grade.INTERN);
    }

    @Test
    void gradeFromExperienceWhenTitleIsSilent() {
        assertThat(VacancyClassifier.grade("Java-разработчик", Experience.NO_EXPERIENCE)).isEqualTo(Grade.JUNIOR);
        assertThat(VacancyClassifier.grade("Java-разработчик", Experience.FROM_1_TO_3)).isEqualTo(Grade.MIDDLE);
        assertThat(VacancyClassifier.grade("Java-разработчик", null)).isNull();
        // "Headless" не должен считаться "Head"
        assertThat(VacancyClassifier.grade("Java developer (headless CMS)", null)).isNull();
    }

    @Test
    void specializationFromTitle() {
        assertThat(VacancyClassifier.specialization("Java Developer")).isEqualTo(Specialization.BACKEND);
        assertThat(VacancyClassifier.specialization("Fullstack Java/React")).isEqualTo(Specialization.FULLSTACK);
        assertThat(VacancyClassifier.specialization("Android-разработчик (Java/Kotlin)")).isEqualTo(Specialization.MOBILE);
        assertThat(VacancyClassifier.specialization("AQA Java")).isEqualTo(Specialization.QA);
    }

    @Test
    void skillWordBoundaries() {
        assertThat(SkillMatcher.containsWord("опыт с javascript", "java")).isFalse();
        assertThat(SkillMatcher.containsWord("java-разработчик", "java")).isTrue();
        assertThat(SkillMatcher.containsWord("знание c++ и c#", "c++")).isTrue();
        assertThat(SkillMatcher.containsWord("работа с gitlab", "git")).isFalse();
    }
}
