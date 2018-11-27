package ada.client.output;

public interface Output {

    void exception(Throwable e);

    void message(String message, Object... args);

    void message(String message);

    void separator();

    void table(String[] headers, String[][] content);

}
