package ada.vcs.client.commands.context;

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
import ada.vcs.server.adapters.directives.ServerDirectivesFactory;
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

  public AdaConfigurationFactory configurationFactory() {
    return AdaConfigurationFactory.apply(om);
  }

  public DatasetFactory datasetFactory() {
    return DatasetFactory.apply(om, DataSourceFactory.apply(), DataSinkFactory.apply(), remoteSourceFactory());
  }

  public AdaProjectFactory projectFactory() {
    return AdaProjectFactory.apply(
      configurationFactory(), remotesFactory(), datasetFactory(),
      AdaHome.apply(configurationFactory()));
  }

  public RemotesFactory remotesFactory() {
    return RemotesFactory.apply(om, systemSupplier.get(), materializerSupplier.get(), versionFactory(), repositoryFactory());
  }

  public RemoteSourceFactory remoteSourceFactory() {
    return RemoteSourceFactory.apply(om, versionFactory(), remotesFactory());
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
      return ServerDirectivesFactory.apply(versionFactory());
  }

  public VersionFactory versionFactory() {
    return VersionFactory.apply(om);
  }

}
