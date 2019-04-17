package ada.vcs.client.commands;

import ada.commons.util.ResourceName;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.converters.api.DataSink;
import ada.vcs.client.converters.api.DataSource;
import ada.vcs.client.converters.internal.monitors.NoOpMonitor;
import ada.vcs.client.core.project.AdaProject;
import ada.vcs.client.core.FileSystemDependent;
import ada.vcs.client.core.dataset.Target;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CommandLine.Command(
    name = "extract",
    description = "extract a dataset to one or more targets")
@AllArgsConstructor(staticName = "apply")
public final class Dataset$Extract extends StandardOptions implements ProjectCommand {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Dataset dataset;

    @CommandLine.Parameters(
        index = "0",
        paramLabel = "TARGETS",
        description = "the alias for the targets to be extracted; if empty all targets will be extracted")
    private List<String> targets;

    public static Dataset$Extract apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null, Lists.newArrayList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(AdaProject project) {
        final Dataset dataset = getDataset().orElseThrow(() -> new IllegalStateException(""));

        if (targets == null || targets.isEmpty()) {
            targets = project
                .getTargets(dataset.getAlias())
                .map(Target::getAlias)
                .map(ResourceName::getValue)
                .collect(Collectors.toList());
        }

        if (targets.isEmpty()) {
            console.message("Dataset '%s' does not contain any targets to extract data.", dataset.getAlias());
            return;
        }


        context.withMaterializer(materializer -> {
            ada.vcs.client.core.dataset.Dataset ds = project.getDataset(dataset.getAlias());

            DataSource<?> source = ds.getSource();
            if (source instanceof FileSystemDependent) {
                source = ((FileSystemDependent<? extends DataSource>) source).resolve(project.getPath());
            }

            console.message("Extracting data to %d target(s)", targets.size());

            return source
                .analyze(materializer, ds.getSchema())
                .thenCompose(src -> Source
                    .from(targets)
                    .zipWithIndex()
                    .mapAsync(1, pair -> {
                        String target = pair.first();
                        Long idx = (Long) pair.second();

                        console.message(
                            "-> Starting to extract dataset '%s' to target '%s' (%d of %d).",
                            ds.getAlias().getValue(), target, idx + 1, targets.size());

                        DataSink sink = project
                            .getTarget(dataset.getAlias(), target)
                            .getSink();

                        if (sink instanceof FileSystemDependent) {
                            sink = ((FileSystemDependent<? extends DataSink>) sink).resolve(project.getPath());
                        }

                        return src
                            .getRecords(NoOpMonitor.apply())
                            .runWith(sink.sink(src.getSchema()), materializer)
                            .thenApply(summary -> {
                                console.message(
                                    "   Done. Extracted %d records from dataset '%s' to target '%s'.",
                                    summary.getCount(), ds.getAlias().getValue(), target);

                                return summary;
                            });
                    })
                    .runWith(Sink.ignore(), materializer));
        });
    }

    public Optional<Dataset> getDataset() {
        return Optional.ofNullable(dataset);
    }

}
