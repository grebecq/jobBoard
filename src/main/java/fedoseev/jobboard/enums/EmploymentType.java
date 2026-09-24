package fedoseev.jobboard.enums;

/**
 * Тип занятости. Удалёнка сюда не относится — это WorkFormat.
 */
public enum EmploymentType implements LabeledEnum {
    FULL_TIME("Полная занятость"),
    PART_TIME("Частичная занятость"),
    PROJECT("Проектная работа"),
    INTERNSHIP("Стажировка");

    private final String label;

    EmploymentType(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
