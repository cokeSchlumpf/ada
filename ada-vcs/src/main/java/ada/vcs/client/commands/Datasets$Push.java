package ada.vcs.client.commands;

import ada.commons.util.ResourceName;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.converters.internal.api.DataSource;
import ada.vcs.client.converters.internal.monitors.NoOpMonitor;
import ada.vcs.client.core.Dataset;
import ada.vcs.client.core.FileSystemDependent;
import ada.vcs.client.core.project.AdaProject;
import ada.vcs.client.core.remotes.Remote;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import picocli.CommandLine;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(
    name = "push",
    description = "push all datasets to a remote repository",
    subcommands = {
        Datasets$Add$CSV.class
    })
public final class Datasets$Push extends StandardOptions implements ProjectCommand {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.Parameters(
        index = "0",
        arity = "0..1",
        paramLabel = "REMOTE",
        description = "the name of the remote to be pushed")
    private String remote;

    @CommandLine.Option(
        names = { "-u", "--set-upstream" },
        description = "if set, the remote will be set as default upstream remote")
    private boolean setUpstream;

    private Datasets$Push(CommandLineConsole console, CommandContext context) {
        this.console = console;
        this.context = context;
    }

    public static Datasets$Push apply(CommandLineConsole console, CommandContext context) {
        return new Datasets$Push(console, context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(AdaProject project) {
        List<Dataset> datasets = project
            .getDatasets()
            .collect(Collectors.toList());

        if (remote == null || remote.trim().length() == 0) {
            remote = project
                .getUpstream()
                .map(Remote::getAlias)
                .map(ResourceName::getValue)
                .orElse(null);

            if (remote == null) {
                console.message("No remote provided and no upstream set.");
                return;
            }
        }

        if (datasets.isEmpty()) {
            console.message("The project does not contain any datasets to push.");
            return;
        }

        final Remote rm = Stream
            .of(project.getRemote(remote))
            .map(rem -> {
                if (setUpstream) {
                    project.updateUpstream(rem.getAlias().getValue());
                }

                if (rem instanceof FileSystemDependent) {
                    return ((FileSystemDependent<? extends Remote>) rem).resolve(project.getPath());
                } else {
                    return rem;
                }
            })
            .findFirst()
            .get();

        console.message("Pushing %d dataset(s) to remote '%s'", datasets.size(), rm.getAlias().getValue());

        context
            .withMaterializer(materializer -> Source
                .from(datasets)
                .zipWithIndex()
                .map(pair -> {
                    Dataset dataset = pair.first();
                    Long idx = (Long) pair.second();

                    console.message(
                        "-> Uploading data to from dataset '%s' (%d of %d).",
                        dataset.getAlias().getValue(), idx + 1, datasets.size());

                    if (dataset.getSource() instanceof FileSystemDependent) {
                        return dataset.withSource(
                            ((FileSystemDependent<? extends DataSource>) dataset.getSource()).resolve(project.getPath()));
                    } else {
                        return dataset;
                    }
                })
                .mapAsync(1, dataset -> dataset
                    .getSource()
                    .analyze(materializer, dataset.getSchema())
                    .thenCompose(readableDataSource ->
                        readableDataSource
                            .getRecords(NoOpMonitor.apply())
                            .toMat(rm.push(dataset.getSchema()), (l, r) -> l.thenCompose(i -> r))
                            .run(materializer)
                            .thenApply(summary -> {
                                console.message("   Done.");
                                return summary;
                            })))
                .runWith(Sink.ignore(), materializer));
    }

}
