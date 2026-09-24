package fedoseev.jobboard.enums;

public enum SkillCategory implements LabeledEnum {
    LANGUAGE("Языки программирования"),
    BACKEND("Бэкенд"),
    FRONTEND("Фронтенд"),
    MOBILE("Мобильная разработка"),
    DATABASE("Базы данных"),
    MESSAGING("Брокеры сообщений"),
    DEVOPS("DevOps и инфраструктура"),
    CLOUD("Облака"),
    TESTING("Тестирование"),
    DATA_ML("Data Science и ML"),
    GAMEDEV("Геймдев"),
    SECURITY("Безопасность"),
    TOOLS("Инструменты"),
    PRACTICES("Практики и подходы"),
    OTHER("Другое");

    private final String label;

    SkillCategory(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
