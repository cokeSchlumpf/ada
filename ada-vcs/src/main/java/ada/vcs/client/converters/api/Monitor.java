package ada.vcs.client.converters.api;

public interface Monitor {

    void processed();

    void warning(long record, String field, String message);

}
