package pl.edu.icm.board.model;

public enum EducationLevel {
    K("kindergarten"),
    P("primary school"),
    H("high school"),
    PH("primary and high school"),
    BU("big university"),
    U("university");

    private final String name;

    EducationLevel(String name) {
        this.name = name;
    }

    public boolean isUniversity() {
        return this == U || this == BU;
    }
}
