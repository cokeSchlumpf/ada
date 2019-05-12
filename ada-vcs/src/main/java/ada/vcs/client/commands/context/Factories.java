package ada.vcs.client.commands.context;

import ada.vcs.client.core.endpoints.EndpointFactory;
import ada.vcs.server.adapters.client.modifiers.AuthenticationMethodFactory;
import ada.vcs.server.adapters.client.repositories.RepositoriesClientFactory;
import ada.vcs.shared.converters.api.DataSinkFactory;
import ada.vcs.shared.converters.api.DataSourceFactory;
import ada.vcs.client.core.AdaHome;
import ada.vcs.client.core.configuration.AdaConfigurationFactory;
import ada.vcs.client.core.dataset.DatasetFactory;
import ada.vcs.client.core.dataset.RemoteSourceFactory;
import ada.vcs.client.core.project.AdaProjectFactory;
import ada.vcs.client.core.remotes.RemotesFactory;
import ada.vcs.shared.repository.api.RepositorySinkFactory;
import ada.vcs.shared.repository.api.RepositorySourceFactory;
import ada.vcs.shared.repository.api.version.VersionFactory;
import ada.vcs.shared.repository.fs.FileSystemRepositoryFactory;
import ada.vcs.server.adapters.server.directives.ServerDirectivesFactory;
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
        return EndpointFactory.apply(authenticationMethodFactory());
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
