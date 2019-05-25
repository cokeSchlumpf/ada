package ada.adapters.cli.util;

import ada.commons.util.FileSize;
import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@AllArgsConstructor(staticName = "apply")
public final class TestDataFactory {

    private final Schema schema;

    private final Lorem lorem;

    private final Random random;

    public static TestDataFactory apply() {
        Schema schema = SchemaBuilder
            .builder()
            .record("test")
            .fields()
            .requiredString("some_string")
            .optionalString("some_other_string")
            .requiredLong("some_number")
            .requiredDouble("some_other_number")
            .endRecord();

        return apply(schema, LoremIpsum.getInstance(), new Random());
    }

    public GenericRecord randomRecord() {
        GenericRecordBuilder grb = new GenericRecordBuilder(schema);

        grb.set("some_string", lorem.getFirstName());
        grb.set("some_other_string", lorem.getEmail());
        grb.set("some_number", random.nextLong());
        grb.set("some_other_number", random.nextDouble());

        return grb.build();
    }

    public Stream<GenericRecord> getRecordsAsStream() {
        return getRecordsAsStream(100);
    }

    public Stream<GenericRecord> getRecordsAsStream(int count) {
        return IntStream
            .range(1, count + 1)
            .mapToObj(i -> randomRecord());
    }

    public Source<GenericRecord, NotUsed> getRecordsAsSource() {
        return getRecordsAsSource(100);
    }

    public Source<GenericRecord, NotUsed> getRecordsAsSource(int count) {
        return Source
            .range(1, count)
            .map(i -> randomRecord());
    }

    public static Path createSampleCSVFile(Path path) {
        return createSampleCSVFile(path, "sample.csv");
    }

    public static Path createSampleCSVFile(Path path, String filename) {
        String csvContent = TemplatesUtil.renderTemplateFromResources("/samples/sample-01-data.csv");
        return createSampleCSVFile(path, filename, FileSize.apply(csvContent.length(), FileSize.Unit.BYTES));
    }

    public static Path createSampleCSVFile(Path path, String filename, FileSize size) {
        final Path file = path.resolve(filename);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file.toFile(), false))) {
            String header = TemplatesUtil.renderTemplateFromResources("/samples/sample-01-header.csv");
            String csvContent = TemplatesUtil.renderTemplateFromResources("/samples/sample-01-data.csv");

            int contentSize = csvContent.length();
            long factor = size.getBytes() / contentSize + 1;

            writer.print(header);

            for (int i = 0; i < factor; i++){
                writer.print(csvContent);
            }
            return file;
        } catch (IOException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    public Schema getSchema() {
        return schema;
    }
}
