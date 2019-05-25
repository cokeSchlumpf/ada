package ada.adapters.server.directives;

import ada.commons.util.ResourceName;
import ada.commons.io.Writable;
import ada.domain.dvc.values.User;
import ada.domain.dvc.values.repository.RefSpec;
import ada.domain.dvc.values.repository.version.VersionDetails;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.Route;
import akka.japi.function.Function;
import akka.japi.function.Function2;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

import java.util.concurrent.CompletionStage;

public interface ServerDirectives {

    <T> Route jsonEntity(Class<T> type, Function<T, Route> next);

    Route complete(Source<ByteString, CompletionStage<VersionDetails>> data);

    <T> Route complete(T result);

    <T> Route complete(CompletionStage<T> result);

    <T extends Writable> Route complete(T result);

    ExceptionHandler exceptionHandler();

    <T extends Writable> Route onSuccess(CompletionStage<T> result);

    Route refSpec(Function<RefSpec, Route> next);

    Route resource(Function<ResourceName, Route> next);

    Route records(Function2<VersionDetails, Source<ByteString, CompletionStage<VersionDetails>>, CompletionStage<Route>> next);

    Route user(Function<User, Route> next);

}
