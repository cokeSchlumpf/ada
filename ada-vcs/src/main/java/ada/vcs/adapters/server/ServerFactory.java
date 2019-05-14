package ada.vcs.adapters.server;

import ada.commons.util.ActorPatterns;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.domain.dvc.DataVersionControlPersisted;
import ada.vcs.domain.dvc.entities.Sum;
import ada.vcs.domain.dvc.protocol.api.DataVersionControlMessage;
import ada.vcs.domain.shared.repository.fs.FileSystemRepositorySettings;
import ada.vcs.domain.shared.repository.fs.FileSystemRepositoryStorageAdapter;
import ada.vcs.adapters.server.directives.ServerDirectives;
import ada.vcs.api.RepositoriesResource;
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
            .spawn(context.system(), DataVersionControlPersisted.createBehavior(context, storageAdapter), "repositories");

        final ActorRef<Sum.Command> sumActor = Adapter
            .spawn(context.system(), Sum.createBehavior(), "sum");

        sumActor.tell(Sum.Add.apply(3));

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
