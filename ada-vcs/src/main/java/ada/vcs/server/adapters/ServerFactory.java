package ada.vcs.server.adapters;

import ada.commons.util.ActorPatterns;
import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.shared.repository.fs.FileSystemRepositorySettings;
import ada.vcs.shared.repository.fs.FileSystemRepositoryStorageAdapter;
import ada.vcs.server.adapters.directives.ServerDirectives;
import ada.vcs.server.domain.repository.Protocol;
import ada.vcs.server.domain.repository.DataVersionControl;
import ada.vcs.server.api.RepositoriesResource;
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

        final ActorRef<Protocol.DataVersionControlMessage> repositoriesActor = Adapter
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
