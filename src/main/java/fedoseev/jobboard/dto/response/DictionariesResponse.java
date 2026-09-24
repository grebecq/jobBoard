package fedoseev.jobboard.dto.response;

import fedoseev.jobboard.enums.LabeledEnum;

import java.util.Arrays;
import java.util.List;

public record DictionariesResponse(
        List<Option> specializations,
        List<Option> grades,
        List<Option> experiences,
        List<Option> workFormats,
        List<Option> employmentTypes,
        List<Option> skillCategories,
        List<SkillResponse> skills
) {

    public record Option(String value, String label) {

        public static <E extends Enum<E> & LabeledEnum> List<Option> of(Class<E> type) {
            return Arrays.stream(type.getEnumConstants())
                    .map(e -> new Option(e.name(), e.getLabel()))
                    .toList();
        }
    }
}
