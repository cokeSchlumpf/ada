package ada.domain.dvc.values.repository;

import ada.domain.dvc.values.repository.version.VersionDetails;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

@FunctionalInterface
public interface RepositorySink extends Supplier<Sink<ByteString, CompletionStage<VersionDetails>>> {

}
