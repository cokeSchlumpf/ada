package ada.adapters.cli.commands.context;

import ada.adapters.cli.core.configuration.AdaConfigurationFactory;
import ada.adapters.client.modifiers.AuthenticationMethodFactory;
import ada.adapters.client.repositories.RepositoriesClientFactory;
import ada.adapters.server.directives.ServerDirectivesFactory;
import ada.domain.legacy.converters.api.DataSinkFactory;
import ada.domain.legacy.converters.api.DataSourceFactory;
import ada.domain.legacy.repository.api.RepositorySinkFactory;
import ada.domain.legacy.repository.api.version.VersionFactory;
import ada.domain.legacy.repository.fs.FileSystemRepositoryFactory;
import ada.adapters.cli.core.endpoints.EndpointFactory;
import ada.adapters.cli.core.AdaHome;
import ada.adapters.cli.core.dataset.DatasetFactory;
import ada.adapters.cli.core.dataset.RemoteSourceFactory;
import ada.adapters.cli.core.project.AdaProjectFactory;
import ada.adapters.cli.core.remotes.RemotesFactory;
import ada.domain.legacy.repository.api.RepositorySourceFactory;
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
