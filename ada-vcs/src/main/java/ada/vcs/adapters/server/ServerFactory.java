package ada.vcs.adapters.server;

import ada.commons.util.ActorPatterns;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.adapters.server.directives.ServerDirectives;
import ada.vcs.api.RepositoriesResource;
import ada.vcs.domain.dvc.DataVersionControl;
import ada.vcs.domain.dvc.protocol.api.DataVersionControlMessage;
import ada.vcs.domain.legacy.repository.fs.FileSystemRepositorySettings;
import ada.vcs.domain.legacy.repository.fs.FileSystemRepositoryStorageAdapter;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.Adapter;
import lombok.AllArgsConstructor;

import java.nio.file.Path;

@AllArgsConstructor(staticName = "apply")
public final class ServerFactory {

    private final CommandContext context;

    public Server create(Path repositoryRootDirectory) {
        final FileSystemRepositorySettings settings = context
            .factories()
            .repositoryFactory()
            .createSettingsBuilder()
            .build();

        final FileSystemRepositoryStorageAdapter storageAdapter = FileSystemRepositoryStorageAdapter
            .apply(settings, repositoryRootDirectory);

        final ActorRef<DataVersionControlMessage> repositoriesActor = Adapter
            .spawn(context.system(), DataVersionControl.createBehavior(context, storageAdapter), "repositories");

        final RepositoriesResource repositories = RepositoriesResource.apply(
            repositoriesActor, context.factories().repositorySinkFactory(),
            context.factories().repositorySourceFactory(), context.system(), ActorPatterns.apply(context.system()));

        final ServerDirectives directives = context
            .factories()
            .serverDirectivesFactory()
            .create(repositoryRootDirectory);

        return Server.apply(directives, repositories);
    }

}
