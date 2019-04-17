package ada.vcs.client.converters.api;

import ada.vcs.client.converters.csv.CSVSource;
import ada.vcs.client.converters.csv.CSVSourceMemento;

public final class DataSourceFactory {

    public static DataSource<? extends Context> createDataSource(DataSourceMemento memento) {
        if (memento instanceof CSVSourceMemento) {
            return CSVSource.apply((CSVSourceMemento) memento);
        } else {
            String message = String.format("Unknown DataSourceMemento `%s`", memento.getClass());
            throw new IllegalArgumentException(message);
        }
    }

}
