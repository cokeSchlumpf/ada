package ada.vcs.client.core.repository.api;

import ada.vcs.client.core.repository.api.version.VersionDetails;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

@FunctionalInterface
public interface RepositorySource extends Supplier<Source<ByteString, CompletionStage<VersionDetails>>> {

}
