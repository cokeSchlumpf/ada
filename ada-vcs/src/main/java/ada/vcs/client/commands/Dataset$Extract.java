package ada.vcs.client.commands;

import ada.commons.util.ResourceName;
import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.converters.api.DataSink;
import ada.vcs.client.converters.api.ReadableDataSource;
import ada.vcs.client.converters.internal.monitors.NoOpMonitor;
import ada.vcs.client.core.dataset.Target;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@CommandLine.Command(
    name = "extract",
    description = "extract a dataset to one or more targets")
@AllArgsConstructor(staticName = "apply")
public final class Dataset$Extract extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Dataset dataset;

    @CommandLine.Parameters(
        index = "0",
        paramLabel = "TARGETS",
        description = "the alias for the targets to be extracted; if empty all targets will be extracted")
    private List<String> targets;

    @CommandLine.Option(
        names = {"--original", "-o"},
        description = "force reading the original source")
    private boolean original;

    public static Dataset$Extract apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null, Lists.newArrayList(), false);
    }

    @Override
    public void run() {
        context.withProject(project -> {
            final Dataset dataset = getDataset().orElseThrow(() -> new IllegalStateException(""));

            if (targets == null || targets.isEmpty()) {
                targets = project
                    .getTargets(dataset.alias())
                    .map(Target::alias)
                    .map(ResourceName::getValue)
                    .collect(Collectors.toList());
            }

            if (targets.isEmpty()) {
                console.message("Dataset '%s' does not contain any targets to extract data", dataset.alias());
                return;
            }

            context.withMaterializer(materializer -> {
                final ada.vcs.client.core.dataset.Dataset ds = project.getDataset(dataset.alias());

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
                        console.message(
                            "Extracting dataset '%s' from '%s' to %d target(s)",
                            ds.alias().getValue(), src.info(), targets.size());

                        return Source
                            .from(targets)
                            .zipWithIndex()
                            .mapAsync(1, pair -> {
                                final String target = pair.first();

                                final Long idx = pair.second();

                                final DataSink sink = project
                                    .getTarget(dataset.alias(), target)
                                    .sink()
                                    .resolve(project.path());

                                console.message(
                                    "-> Starting to extract target '%s' (%d of %d)",
                                    target, idx + 1, targets.size());


                                return src
                                    .getRecords(NoOpMonitor.apply())
                                    .runWith(sink.sink(src.schema()), materializer)
                                    .thenApply(summary -> {
                                        console.message(
                                            "   Extracted %d records to target '%s'",
                                            summary.getCount(), target);

                                        return summary;
                                    });
                            })
                            .runWith(Sink.ignore(), materializer);
                    });
            });
        });
    }

    public Optional<Dataset> getDataset() {
        return Optional.ofNullable(dataset);
    }

}
