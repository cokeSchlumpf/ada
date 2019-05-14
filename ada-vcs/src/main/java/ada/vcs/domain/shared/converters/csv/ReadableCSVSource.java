package ada.vcs.domain.shared.converters.csv;

import ada.vcs.domain.shared.converters.api.Monitor;
import ada.vcs.domain.shared.converters.api.ReadSummary;
import ada.vcs.domain.shared.converters.api.ReadableDataSource;
import ada.vcs.domain.shared.repository.api.RefSpec;
import ada.vcs.domain.shared.datatypes.DataTypeMatcher;
import akka.stream.javadsl.Source;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReadableCSVSource implements ReadableDataSource {

    private final CSVSource source;

    private final Schema schema;

    private final Map<String, DataTypeMatcher> fields;

    private final int offset;

    public static ReadableCSVSource apply(CSVSource source, Schema schema, Map<String, DataTypeMatcher> fields, int offset) {
        for (Schema.Field field : schema.getFields()) {
            if (!fields.containsKey(field.name())) {
                throw new IllegalArgumentException("fields must contain value for each field of the schema");
            }
        }

        return new ReadableCSVSource(source, schema, fields, offset);
    }

    @Override
    public String info() {
        return String.format("file://%s:/%s", source.getFile().toAbsolutePath(), ref());
    }

    @Override
    public Schema schema() {
        return schema;
    }

    @Override
    public RefSpec.VersionRef ref() {
        return RefSpec.fromFile(source.getFile());
    }

    @Override
    public Source<GenericRecord, CompletionStage<ReadSummary>> getRecords(Monitor monitor) {
        final AtomicLong recordIdx = new AtomicLong();
        final AtomicLong failures = new AtomicLong();
        final AtomicLong success = new AtomicLong();

       final  List<AvroFieldReader> fieldReaders = schema
            .getFields()
            .stream()
            .map(f -> AvroFieldReader.apply(f.name(), f, fields.get(f.name())))
            .collect(Collectors.toList());

        final Function<List<String>, Optional<GenericRecord>> parseRecord = (input) -> {
            List<String> values = input;
            final boolean[] failed = new boolean[]{false};
            final long index = recordIdx.get(); // recordIdx.getAndIncrement();
            final GenericRecordBuilder record = new GenericRecordBuilder(schema);

            // fill missing values with null, this is ok if field is optional (will be checked below)
            if (values.size() < schema.getFields().size()) {
                values = Lists.newArrayList(values);
                int s = values.size();

                for (int i = 0; i < schema.getFields().size() - s; i++) {
                    values.add(null);
                }
            }

            for (int i = 0; i < schema.getFields().size(); i++) {
                Schema.Field field = schema.getFields().get(i);
                String value = values.get(i);
                AvroFieldReader reader = fieldReaders.get(i);

                reader
                    .read(value)
                    .apply(
                        readValue -> {
                            monitor.processed();
                            record.set(field, readValue);
                        },
                        error -> {
                            monitor.warning(index, field.name(), error.getMessage());
                            failed[0] = true;
                        });

                if (failed[0]) break;
            }

            if (failed[0]) {
                // failures.incrementAndGet();
                return Optional.empty();
            } else {
                // success.incrementAndGet();
                return Optional.of((GenericRecord) record.build());
            }
        };

        return source
            .read()
            .drop(offset)
            .async()
            .map(parseRecord::apply)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .watchTermination((ignore, done) -> done.thenApply(d -> ReadSummary.apply(success.get(), failures.get())));
    }


}
