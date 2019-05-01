package ada.vcs.client.core.repository.api;

import ada.vcs.client.core.repository.api.version.VersionDetails;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

import java.util.concurrent.CompletionStage;

public interface RepositoryDirectAccess extends Repository {

    Sink<ByteString, CompletionStage<VersionDetails>> insert(VersionDetails version);

    Source<ByteString, CompletionStage<VersionDetails>> read(RefSpec refSpec);

}
