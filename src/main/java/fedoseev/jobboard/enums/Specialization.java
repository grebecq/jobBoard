package fedoseev.jobboard.enums;

/**
 * Специализация (направление) вакансии, как «Профессиональная роль» на hh.ru.
 */
public enum Specialization implements LabeledEnum {
    BACKEND("Бэкенд-разработка"),
    FRONTEND("Фронтенд-разработка"),
    FULLSTACK("Fullstack-разработка"),
    MOBILE("Мобильная разработка"),
    GAMEDEV("Разработка игр"),
    EMBEDDED("Встраиваемые системы"),
    ONE_C("1С-разработка"),
    QA("Тестирование (QA)"),
    DEVOPS("DevOps / SRE"),
    SYSADMIN("Системное администрирование"),
    DBA("Администрирование БД"),
    SECURITY("Информационная безопасность"),
    DATA_SCIENCE("Data Science / ML"),
    DATA_ENGINEER("Data Engineering"),
    DATA_ANALYST("Аналитика данных"),
    SYSTEM_ANALYST("Системная и бизнес-аналитика"),
    ARCHITECT("Архитектура"),
    PRODUCT("Продакт-менеджмент"),
    PROJECT("Проджект-менеджмент"),
    DESIGN("UX/UI-дизайн"),
    TECH_WRITER("Техническое писательство"),
    SUPPORT("Техническая поддержка");

    private final String label;

    Specialization(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
