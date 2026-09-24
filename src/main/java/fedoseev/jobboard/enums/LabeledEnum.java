package fedoseev.jobboard.enums;

/**
 * Enum с человекочитаемой подписью — фронт получает подписи из /api/dictionaries, а не хардкодит их.
 */
public interface LabeledEnum {

    String name();

    String getLabel();
}
