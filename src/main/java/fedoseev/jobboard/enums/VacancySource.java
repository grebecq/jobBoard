package fedoseev.jobboard.enums;

public enum VacancySource implements LabeledEnum {
    JOBBOARD("jobBoard"),
    HH("hh.ru");

    private final String label;

    VacancySource(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
