package fedoseev.jobboard.enums;

public enum Experience implements LabeledEnum {
    NO_EXPERIENCE("Без опыта"),
    FROM_1_TO_3("От 1 года до 3 лет"),
    FROM_3_TO_6("От 3 до 6 лет"),
    MORE_THAN_6("Более 6 лет");

    private final String label;

    Experience(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
