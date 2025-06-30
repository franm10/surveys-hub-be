package sdcc.surveyshub.utils.enums;

public enum Status {

    OPEN("open"),

    CLOSED("closed"),

    ARCHIVED("archived");

    private final String description;

    Status(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }

}
