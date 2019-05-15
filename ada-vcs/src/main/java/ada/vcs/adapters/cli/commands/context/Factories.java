package ada.vcs.adapters.cli.commands.context;

import ada.vcs.adapters.cli.core.endpoints.EndpointFactory;
import ada.vcs.adapters.client.modifiers.AuthenticationMethodFactory;
import ada.vcs.adapters.client.repositories.RepositoriesClientFactory;
import ada.vcs.domain.legacy.converters.api.DataSinkFactory;
import ada.vcs.domain.legacy.converters.api.DataSourceFactory;
import ada.vcs.adapters.cli.core.AdaHome;
import ada.vcs.adapters.cli.core.configuration.AdaConfigurationFactory;
import ada.vcs.adapters.cli.core.dataset.DatasetFactory;
import ada.vcs.adapters.cli.core.dataset.RemoteSourceFactory;
import ada.vcs.adapters.cli.core.project.AdaProjectFactory;
import ada.vcs.adapters.cli.core.remotes.RemotesFactory;
import ada.vcs.domain.legacy.repository.api.RepositorySinkFactory;
import ada.vcs.domain.legacy.repository.api.RepositorySourceFactory;
import ada.vcs.domain.legacy.repository.api.version.VersionFactory;
import ada.vcs.domain.legacy.repository.fs.FileSystemRepositoryFactory;
import ada.vcs.adapters.server.directives.ServerDirectivesFactory;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.util.function.Supplier;

@AllArgsConstructor(staticName = "apply")
public final class Factories {

    private final ObjectMapper om;

    private final Supplier<ActorSystem> systemSupplier;

    private final Supplier<Materializer> materializerSupplier;

    public AuthenticationMethodFactory authenticationMethodFactory() {
        return AuthenticationMethodFactory.apply();
    }

    public AdaConfigurationFactory configurationFactory() {
        return AdaConfigurationFactory.apply(om, endpointFactory());
    }

    public DatasetFactory datasetFactory() {
        return DatasetFactory.apply(om, DataSourceFactory.apply(), DataSinkFactory.apply(), remoteSourceFactory());
    }

    public EndpointFactory endpointFactory() {
        return EndpointFactory.apply(authenticationMethodFactory(), repositoriesClientFactory());
    }

    public ObjectMapper objectMapper() {
        return om;
    }

    public AdaProjectFactory projectFactory() {
        return AdaProjectFactory.apply(
            configurationFactory(), remotesFactory(), datasetFactory(),
            AdaHome.apply(configurationFactory()));
    }

    public RemotesFactory remotesFactory() {
        return RemotesFactory.apply(
            om, systemSupplier.get(), materializerSupplier.get(),
            versionFactory(), repositoryFactory(), repositoriesClientFactory());
    }

    public RemoteSourceFactory remoteSourceFactory() {
        return RemoteSourceFactory.apply(om, versionFactory(), remotesFactory());
    }

    public RepositoriesClientFactory repositoriesClientFactory() {
        return RepositoriesClientFactory.apply(systemSupplier.get(), materializerSupplier.get(), om, versionFactory());
    }

    public RepositorySinkFactory repositorySinkFactory() {
        return RepositorySinkFactory.apply(om, versionFactory());
    }

    public RepositorySourceFactory repositorySourceFactory() {
        return RepositorySourceFactory.apply(om, versionFactory(), materializerSupplier.get());
    }

    public FileSystemRepositoryFactory repositoryFactory() {
        return FileSystemRepositoryFactory.apply(om, materializerSupplier.get(), versionFactory());
    }

    public ServerDirectivesFactory serverDirectivesFactory() {
        return ServerDirectivesFactory.apply(versionFactory(), om);
    }

    public VersionFactory versionFactory() {
        return VersionFactory.apply(om);
    }

}
