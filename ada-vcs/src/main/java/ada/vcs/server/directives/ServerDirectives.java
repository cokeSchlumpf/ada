package ada.vcs.server.directives;

import ada.commons.util.ResourceName;
import ada.vcs.client.core.Writable;
import ada.vcs.client.core.repository.api.RefSpec;
import ada.vcs.client.core.repository.api.version.VersionDetails;
import akka.http.javadsl.server.Route;
import akka.japi.function.Function;
import akka.japi.function.Function2;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

import java.util.concurrent.CompletionStage;

public interface ServerDirectives {

    Route complete(Source<ByteString, CompletionStage<VersionDetails>> data);

    <T extends Writable> Route complete(T result);

    <T extends Writable> Route onSuccess(CompletionStage<T> result);

    Route refSpec(Function<RefSpec, Route> next);

    Route resource(Function<ResourceName, Route> next);

    Route records(Function2<VersionDetails, Source<ByteString, CompletionStage<VersionDetails>>, CompletionStage<Route>> next);

}
