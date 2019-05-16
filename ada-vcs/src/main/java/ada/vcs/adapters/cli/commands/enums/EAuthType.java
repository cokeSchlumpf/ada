package ada.vcs.adapters.cli.commands.enums;

public enum EAuthType {

    USER("user"),
    ROLE("role"),
    WILDCARD("all");

    private final String value;

    EAuthType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

}
