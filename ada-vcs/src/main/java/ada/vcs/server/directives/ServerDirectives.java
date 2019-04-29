package ada.vcs.server.directives;

import ada.vcs.client.core.Writable;
import ada.vcs.client.core.repository.api.Repository;
import ada.vcs.client.core.repository.api.version.VersionDetails;
import akka.NotUsed;
import akka.http.javadsl.server.Route;
import akka.japi.function.Function;
import akka.japi.function.Function2;
import akka.stream.javadsl.Source;
import org.apache.avro.generic.GenericRecord;

import java.util.concurrent.CompletionStage;

public interface ServerDirectives {

    <T extends Writable> Route complete(T result);

    <T extends Writable> Route onSuccess(CompletionStage<T> result);

    Route repository(Function<Repository, Route> next);

    Route records(Function2<VersionDetails, Source<GenericRecord, CompletionStage<VersionDetails>>, CompletionStage<Route>> next);

}
