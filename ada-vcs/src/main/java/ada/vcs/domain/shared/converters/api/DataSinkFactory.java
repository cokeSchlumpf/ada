package ada.vcs.domain.shared.converters.api;

import ada.vcs.domain.shared.converters.avro.AvroSink;
import ada.vcs.domain.shared.converters.avro.AvroSinkMemento;
import ada.vcs.domain.shared.converters.csv.CSVSink;
import ada.vcs.domain.shared.converters.csv.CSVSinkMemento;
import ada.vcs.domain.shared.converters.local.LocalSink;
import ada.vcs.domain.shared.converters.local.LocalSinkMemento;
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
