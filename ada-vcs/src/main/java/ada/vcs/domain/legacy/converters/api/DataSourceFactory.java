package ada.vcs.domain.legacy.converters.api;

import ada.vcs.domain.legacy.converters.csv.CSVSource;
import ada.vcs.domain.legacy.converters.csv.CSVSourceMemento;
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
