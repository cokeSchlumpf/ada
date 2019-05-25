package ada.adapters.cli.converters.api;

import ada.adapters.cli.converters.csv.CSVSourceMemento;
import ada.adapters.cli.converters.csv.CSVSource;
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
