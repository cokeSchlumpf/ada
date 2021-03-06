package ada.adapters.server;

import ada.adapters.cli.commands.context.CommandContext;
import ada.adapters.server.directives.ServerDirectives;
import ada.api.RepositoriesResource;
import ada.commons.Configs;
import ada.commons.util.ActorPatterns;
import ada.domain.dvc.DataVersionControl;
import ada.domain.dvc.protocol.api.DataVersionControlMessage;
import ada.adapters.cli.repository.fs.FileSystemRepositorySettings;
import ada.domain.dvc.values.repository.fs.FileSystemRepositoryStorageAdapter;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.Adapter;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.management.javadsl.AkkaManagement;
import lombok.AllArgsConstructor;

import java.nio.file.Path;

@AllArgsConstructor(staticName = "apply")
public final class ServerFactory {

    private final CommandContext context;

    public Server create(Path repositoryRootDirectory) {
        if (Configs.application.hasPath("akka.discovery.method") &&
            !Configs.application.getString("akka.discovery.method").equals("<method>")) {

            AkkaManagement.get(context.system()).start();
            ClusterBootstrap.get(context.system()).start();
        }

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
