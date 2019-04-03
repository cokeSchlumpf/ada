package ada.vcs.client.converters.csv;

import ada.vcs.client.converters.internal.api.Monitor;
import ada.vcs.client.converters.internal.api.ReadSummary;
import ada.vcs.client.converters.internal.api.ReadableDataSource;
import ada.vcs.client.converters.internal.contexts.FileContext;
import ada.vcs.client.datatypes.DataTypeMatcher;
import akka.NotUsed;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReadableCSVSource implements ReadableDataSource<FileContext> {

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
    public boolean hasDifference(FileContext context) {
        if (context == null) {
            return true;
        } else {
            return !FileContext.apply(source.getFile()).equals(context);
        }
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public Source<GenericRecord, CompletionStage<ReadSummary<FileContext>>> getRecords(Monitor monitor, FileContext context) {
        if (!hasDifference(context)) {
            Source<GenericRecord, NotUsed> seed = Source.empty();
            return seed.mapMaterializedValue(ignore -> CompletableFuture.completedFuture(ReadSummary.apply(context, 0, 0)));
        } else {
            final AtomicLong recordIdx = new AtomicLong();
            final AtomicLong failures = new AtomicLong();
            final AtomicLong success = new AtomicLong();

            List<AvroFieldReader> fieldReaders = schema
                .getFields()
                .stream()
                .map(f -> AvroFieldReader.apply(f.name(), f, fields.get(f.name())))
                .collect(Collectors.toList());

            return source
                .read()
                .drop(offset)
                .map(values -> {
                    final boolean[] failed = new boolean[]{false};
                    final long index = recordIdx.getAndIncrement();
                    final GenericRecordBuilder record = new GenericRecordBuilder(schema);

                    // fill missing values with null, this is ok if field is optional (will be checked below)
                    if (values.size() < schema.getFields().size()) {
                        values = Lists.newArrayList(values);

                        for (int i = 0; i < schema.getFields().size() - values.size(); i++) {
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
                        failures.incrementAndGet();
                        return Optional.<GenericRecord>empty();
                    } else {
                        success.incrementAndGet();
                        return Optional.of((GenericRecord) record.build());
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .watchTermination((ignore, done) ->
                    done.thenApply(d -> ReadSummary.apply(FileContext.apply(source.getFile()), success.get(), failures.get())));
        }
    }

    @Override
    public Source<GenericRecord, CompletionStage<ReadSummary<FileContext>>> getRecords(Monitor monitor) {
        return getRecords(monitor, null);
    }


}
