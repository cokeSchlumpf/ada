package ada.vcs.client.converters.api;

import ada.vcs.client.converters.csv.CSVSource;
import ada.vcs.client.converters.csv.CSVSourceMemento;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public final class DataSourceFactory {

    public DataSource createDataSource(DataSourceMemento memento) {
        if (memento instanceof CSVSourceMemento) {
            return CSVSource.apply((CSVSourceMemento) memento);
        } else {
            String message = String.format("Unknown DataSourceMemento `%s`", memento.getClass());
            throw new IllegalArgumentException(message);
        }
    }

}
