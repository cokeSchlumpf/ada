package ada.adapters.cli.datatypes;

public interface DataTypeDetector<T extends DataTypeDetector<T>> {

    Proximity getProximity();

    void hint(String value);

    T withOptional(boolean isOptional);

    Field type(String fieldName);

}
