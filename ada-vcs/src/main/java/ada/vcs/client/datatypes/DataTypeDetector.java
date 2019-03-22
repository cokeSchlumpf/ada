package ada.vcs.client.datatypes;

public interface DataTypeDetector {

    Proximity proximity();

    void hint(String value);

    DataType type();

    static DataTypeDetector apply() {
        return DataTypeDetectorImpl.apply();
    }

}
