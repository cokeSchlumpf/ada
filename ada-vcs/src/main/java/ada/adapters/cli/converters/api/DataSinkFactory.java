package ada.adapters.cli.converters.api;

import ada.adapters.cli.converters.avro.AvroSink;
import ada.adapters.cli.converters.avro.AvroSinkMemento;
import ada.adapters.cli.converters.csv.CSVSink;
import ada.adapters.cli.converters.csv.CSVSinkMemento;
import ada.adapters.cli.converters.local.LocalSink;
import ada.adapters.cli.converters.local.LocalSinkMemento;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public final class DataSinkFactory {

    public DataSink createDataSink(DataSinkMemento memento) {
        if (memento instanceof AvroSinkMemento) {
            return AvroSink.apply((AvroSinkMemento) memento);
        } else if (memento instanceof CSVSinkMemento) {
            return CSVSink.apply((CSVSinkMemento) memento);
        } else if (memento instanceof LocalSinkMemento) {
            return LocalSink.apply((LocalSinkMemento) memento);
        } else {
            String message = String.format("Unknown DataSinkMemento `%s`", memento.getClass());
            throw new IllegalArgumentException(message);
        }
    }

}
