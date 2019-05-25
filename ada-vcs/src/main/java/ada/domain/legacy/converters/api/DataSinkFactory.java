package ada.domain.legacy.converters.api;

import ada.domain.legacy.converters.avro.AvroSink;
import ada.domain.legacy.converters.avro.AvroSinkMemento;
import ada.domain.legacy.converters.csv.CSVSink;
import ada.domain.legacy.converters.csv.CSVSinkMemento;
import ada.domain.legacy.converters.local.LocalSink;
import ada.domain.legacy.converters.local.LocalSinkMemento;
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
