package ada.adapters.cli.commands;

import ada.adapters.cli.commands.context.CommandContext;
import ada.adapters.cli.consoles.CommandLineConsole;
import ada.adapters.cli.exceptions.CommandNotInitializedException;
import ada.adapters.cli.converters.api.ReadSummary;
import ada.adapters.cli.converters.api.ReadableDataSource;
import ada.adapters.cli.converters.csv.CSVSink;
import ada.adapters.cli.converters.internal.monitors.NoOpMonitor;
import akka.stream.javadsl.Source;
import lombok.AllArgsConstructor;
import org.apache.avro.generic.GenericRecord;
import picocli.CommandLine;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@CommandLine.Command(
    name = "print",
    description = "prints records from the dataset to console output")
@AllArgsConstructor(staticName = "apply")
public final class Dataset$Print extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Dataset dataset;

    @CommandLine.Option(
        names = {"--original", "-o"},
        description = "force reading the original source")
    private boolean original;

    @CommandLine.Option(
        names = {"--limit", "-l"},
        defaultValue = "0",
        description = "limits the number of printed records; a limit of 0 will print all")
    private long limit;

    @CommandLine.Option(
        names = {"--offset", "-s"},
        defaultValue = "0",
        description = "offset to start printing records from source")
    private long offset;

    public static Dataset$Print apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null, false, 0, 0);
    }

    @Override
    public void run() {
        context.withProject(project -> {
            final Dataset dataset = getDataset().orElseThrow(CommandNotInitializedException::apply);

            context.withMaterializer(materializer -> {
                final ada.adapters.cli.core.dataset.Dataset ds = project.getDataset(dataset.alias());

                return ds
                    .remoteSource()
                    .map(rs -> CompletableFuture.completedFuture((ReadableDataSource) rs))
                    .filter(rs -> !original)
                    .orElseGet(() -> ds
                        .source()
                        .resolve(project.path())
                        .analyze(materializer, ds.schema())
                        .toCompletableFuture())
                    .thenCompose(src -> {
                        CSVSink sink = CSVSink.apply(console.printStream());

                        Source<GenericRecord, CompletionStage<ReadSummary>> records = src
                            .getRecords(NoOpMonitor.apply());

                        if (offset > 0) {
                            records = records.drop(offset);
                        }

                        if (limit > 0) {
                            records = records.take(limit);
                        }

                        return records
                            .runWith(sink.sink(src.schema()), materializer);
                    });
            });
        });
    }

    public Optional<Dataset> getDataset() {
        return Optional.ofNullable(dataset);
    }

}
