package fedoseev.jobboard.enums;

/**
 * Квалификация кандидата, как на Хабр Карьере.
 */
public enum Grade implements LabeledEnum {
    INTERN("Стажёр"),
    JUNIOR("Junior"),
    MIDDLE("Middle"),
    SENIOR("Senior"),
    LEAD("Lead");

    private final String label;

    Grade(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
