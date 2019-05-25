package ada.adapters.server.directives;

import ada.commons.exceptions.AskCompletionException;
import ada.commons.io.Writable;
import ada.commons.util.ErrorMessage;
import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.domain.dvc.values.AnonymousUser;
import ada.domain.dvc.values.AuthenticatedUser;
import ada.domain.dvc.values.User;
import ada.domain.legacy.repository.api.RefSpec;
import ada.domain.legacy.repository.api.version.VersionDetails;
import ada.domain.legacy.repository.api.version.VersionFactory;
import akka.NotUsed;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.*;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.japi.Pair;
import akka.japi.function.Function;
import akka.japi.function.Function2;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
final class ServerDirectivesImpl extends AllDirectives implements ServerDirectives {

    private final VersionFactory versionFactory;

    private final ObjectMapper om;

    @Override
    public <T> Route complete(T result) {
        return complete(StatusCodes.OK, result, Jackson.marshaller(om));
    }

    @Override
    public <T> Route complete(CompletionStage<T> result) {
        return onSuccess(result, this::complete);
    }

    @Override
    public <T extends Writable> Route complete(T result) {
        return complete(
            StatusCodes.OK,
            HttpEntities.create(
                ContentTypes.APPLICATION_JSON,
                Operators.suppressExceptions(result::writeToString)));
    }

    @Override
    public ExceptionHandler exceptionHandler() {
        return new ExceptionHandlerBuilder()
            .match(Exception.class, e -> e instanceof ErrorMessage || e instanceof AskCompletionException, e -> {
                ErrorMessageServerError error = ErrorMessageServerError.apply(e.getMessage());
                return complete(StatusCodes.BAD_REQUEST, error, Jackson.marshaller(om));
            })
            .matchAny(e -> {
                e.printStackTrace();
                return complete(StatusCodes.INTERNAL_SERVER_ERROR, InternalServerError.apply(), Jackson.marshaller(om));
            })
            .build();
    }

    @Override
    public <T extends Writable> Route onSuccess(CompletionStage<T> result) {
        return super.onSuccess(result, this::complete);
    }

    @Override
    public Route refSpec(Function<RefSpec, Route> next) {
        return path(PathMatchers.remainingPath(), remaining ->
            get(() -> {
                RefSpec refSpec = RefSpec.fromString(remaining.toString());
                return Operators.suppressExceptions(() -> next.apply(refSpec));
            }));
    }

    @Override
    public Route resource(Function<ResourceName, Route> next) {
        return pathPrefix(value ->
            Operators.suppressExceptions(() -> next.apply(ResourceName.apply(value))));
    }

    @Override
    public <T> Route jsonEntity(Class<T> type, Function<T, Route> next) {
        return entity(Jackson.unmarshaller(om, type), value -> Operators.suppressExceptions(() -> next.apply(value)));
    }

    @Override
    public Route complete(Source<ByteString, CompletionStage<VersionDetails>> data) {
        return extractMaterializer(materializer -> {
            Pair<CompletionStage<VersionDetails>, Source<ByteString, NotUsed>> pair = data.preMaterialize(materializer);

            return onSuccess(pair.first(), details -> Operators.suppressExceptions(() -> {
                HttpEntity.Strict detailsField = HttpEntities.create(details.writeToString());

                HttpEntity.IndefiniteLength records = HttpEntities.createIndefiniteLength(
                    ContentTypes.APPLICATION_OCTET_STREAM, data);

                Multipart.FormData formData = Multiparts.createFormDataFromParts(
                    Multiparts.createFormDataBodyPart("details", detailsField),
                    Multiparts.createFormDataBodyPart("records", records));

                return complete(formData.toEntity());
            }));
        });
    }

    @Override
    public Route records(Function2<VersionDetails, Source<ByteString, CompletionStage<VersionDetails>>, CompletionStage<Route>> next) {
        return extractMaterializer(materializer ->
            entity(Unmarshaller.entityToMultipartFormData(), formData -> {
                CompletionStage<RecordsUploadDataCollection<CompletionStage<Route>>> result = formData
                    .getParts()
                    .map(i -> ((Multipart.FormData.BodyPart) i))
                    .runFoldAsync(RecordsUploadDataCollection.empty(), (acc, bodyPart) -> {
                        switch (bodyPart.getName()) {
                            case "records":
                                RecordsUploadDataCollection<CompletionStage<Route>> collection = acc
                                    .process(details -> {
                                        Source<ByteString, CompletionStage<VersionDetails>> data = bodyPart
                                            .getEntity()
                                            .getDataBytes()
                                            .watchTermination((i, done) -> done.thenApply(d -> details));

                                        return next.apply(details, data);
                                    });

                                return collection
                                    .result()
                                    .map(route -> route.thenApply(r -> collection))
                                    .orElseGet(() -> CompletableFuture.completedFuture(collection));

                            case "details":
                                return bodyPart
                                    .getEntity()
                                    .getDataBytes()
                                    .runFold(ByteString.empty(), ByteString::concat, materializer)
                                    .thenApply(ByteString::toByteBuffer)
                                    .thenApply(ByteBuffer::array)
                                    .thenApply(bytes -> Operators.suppressExceptions(() -> versionFactory.createDetails(bytes)))
                                    .thenApply(acc::withDetails);

                            default:
                                bodyPart.getEntity().discardBytes(materializer);
                                return CompletableFuture.completedFuture(acc);
                        }
                    }, materializer);

                return onSuccess(result, r -> r
                    .result()
                    .map(routeCS -> onSuccess(routeCS, route -> route))
                    .orElseGet(() -> complete(StatusCodes.BAD_REQUEST, "Wrong request format")));
            })
        );
    }

    @Override
    public Route user(Function<User, Route> next) {
        return optionalHeaderValueByName("x-user-id", userId ->
            optionalHeaderValueByName("x-user-name", userName ->
                optionalHeaderValueByName("x-user-roles", roles -> {
                    Set<String> rolesS = roles.map(s -> Sets.newHashSet(s.split(","))).orElse(Sets.newHashSet());
                    User user = userId
                        .map(id -> (User) AuthenticatedUser.apply(id, userName.orElse(id), rolesS))
                        .orElseGet(() -> AnonymousUser.apply(rolesS));

                    return Operators.suppressExceptions(() -> next.apply(user));
                })));
    }


}
