package fedoseev.jobboard.enums;

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
