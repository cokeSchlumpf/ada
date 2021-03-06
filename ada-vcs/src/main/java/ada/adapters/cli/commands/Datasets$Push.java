package ada.adapters.cli.commands;

import ada.adapters.cli.consoles.CommandLineConsole;
import ada.adapters.cli.exceptions.NoUserConfiguredException;
import ada.commons.util.ResourceName;
import ada.adapters.cli.commands.context.CommandContext;
import ada.adapters.cli.converters.internal.monitors.NoOpMonitor;
import ada.adapters.cli.core.dataset.Dataset;
import ada.adapters.cli.core.dataset.RemoteSource;
import ada.adapters.cli.core.remotes.Remote;
import ada.domain.dvc.values.repository.RefSpec;
import ada.domain.dvc.values.repository.User;
import ada.domain.dvc.values.repository.version.VersionDetails;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import picocli.CommandLine;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(
    name = "push",
    description = "push all datasets to a remote repository")
public final class Datasets$Push extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.Parameters(
        index = "0",
        arity = "0..1",
        paramLabel = "REMOTE",
        description = "the name of the remote to be pushed")
    private String remote;

    @CommandLine.Option(
        names = {"-u", "--set-upstream"},
        description = "if set, the remote will be set as default upstream remote")
    private boolean setUpstream = false;

    private Datasets$Push(CommandLineConsole console, CommandContext context) {
        this.console = console;
        this.context = context;
    }

    public static Datasets$Push apply(CommandLineConsole console, CommandContext context) {
        return new Datasets$Push(console, context);
    }

    @Override
    public void run() {
        context.withProject(project -> {
            List<Dataset> datasets = project
                .getDatasets()
                .collect(Collectors.toList());

            User user = project
                .getConfiguration()
                .getUser()
                .orElseThrow(NoUserConfiguredException::apply);

            if (remote == null || remote.trim().length() == 0) {
                remote = project
                    .getUpstream()
                    .map(Remote::alias)
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
                .peek(rem -> {
                    if (setUpstream) {
                        project.updateUpstream(rem.alias().getValue());
                    }
                })
                .findFirst()
                .get();

            console.message("Pushing %d dataset(s) to remote '%s'", datasets.size(), rm.alias().getValue());

            context
                .withMaterializer(materializer -> Source
                    .from(datasets)
                    .zipWithIndex()
                    .map(pair -> {
                        Dataset dataset = pair.first();
                        Long idx = pair.second();

                        console.message(
                            "-> Uploading data to from dataset '%s' (%d of %d).",
                            dataset.alias().getValue(), idx + 1, datasets.size());

                        return dataset.withSource(dataset.source().resolve(project.path()));
                    })
                    .mapAsync(1, dataset -> dataset
                        .source()
                        .analyze(materializer, dataset.schema())
                        .thenCompose(readableDataSource -> {
                            VersionDetails details = context
                                .factories()
                                .versionFactory()
                                .createDetails(user, dataset.schema(), readableDataSource.ref());

                            return readableDataSource
                                .getRecords(NoOpMonitor.apply())
                                .toMat(rm.push(details), (l, r) -> l.thenCompose(i -> r))
                                .run(materializer)
                                .thenApply(versionDetails -> {
                                    RemoteSource rs = context.factories().remoteSourceFactory().apply(details, rm);
                                    project.updateRemoteSource(dataset.alias().getValue(), rs);

                                    console.message(
                                        "   Pushed ref '%s' to '%s'.",
                                        RefSpec.VersionRef.apply(versionDetails.id()), rm.alias().getValue());

                                    return versionDetails;
                                });
                        }))
                    .runWith(Sink.ignore(), materializer));
        });
    }

}
