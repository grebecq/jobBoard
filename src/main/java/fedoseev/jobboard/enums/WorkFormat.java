package fedoseev.jobboard.enums;

/**
 * Где работать: офис, гибрид или удалёнка.
 */
public enum WorkFormat implements LabeledEnum {
    OFFICE("В офисе"),
    HYBRID("Гибрид"),
    REMOTE("Удалённо");

    private final String label;

    WorkFormat(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
