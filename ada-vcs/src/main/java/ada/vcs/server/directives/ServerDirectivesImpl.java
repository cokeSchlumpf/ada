package ada.vcs.server.directives;

import ada.commons.util.Operators;
import ada.vcs.client.core.Writable;
import ada.vcs.client.core.repository.api.RefSpec;
import ada.vcs.client.core.repository.api.RepositoryDirectAccess;
import ada.vcs.client.core.repository.api.version.VersionDetails;
import ada.vcs.client.core.repository.api.version.VersionFactory;
import ada.vcs.client.core.repository.fs.FileSystemRepositoryFactory;
import akka.NotUsed;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.japi.Pair;
import akka.japi.function.Function;
import akka.japi.function.Function2;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import lombok.AllArgsConstructor;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
final class ServerDirectivesImpl extends AllDirectives implements ServerDirectives {

    private final Path repositoryRoot;

    private final VersionFactory versionFactory;

    private final FileSystemRepositoryFactory repositoryFactory;

    @Override
    public <T extends Writable> Route complete(T result) {
        return complete(
            StatusCodes.OK,
            HttpEntities.create(
                ContentTypes.APPLICATION_JSON,
                Operators.suppressExceptions(result::writeToString)));
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

    public Route repository(Function<RepositoryDirectAccess, Route> next) {
        return pathPrefix(alias -> {
            RepositoryDirectAccess repository = repositoryFactory.create(repositoryRoot.resolve(alias));

            try {
                return next.apply(repository);
            } catch (Exception e) {
                e.printStackTrace();
                return complete(StatusCodes.INTERNAL_SERVER_ERROR, "Something went wrong ...");
            }
        });
    }

    @Override
    public Route fromRecords(Source<ByteString, CompletionStage<VersionDetails>> data) {
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
    public Route pushRecords(Function2<VersionDetails, Source<ByteString, CompletionStage<VersionDetails>>, CompletionStage<Route>> next) {
        return extractMaterializer(materializer ->
            entity(Unmarshaller.entityToMultipartFormData(), formData -> {
                CompletionStage<RecordsUploadDataCollection<CompletionStage<Route>>> result = formData
                    .getParts()
                    .map(i -> ((Multipart.FormData.BodyPart) i))
                    .runFoldAsync(RecordsUploadDataCollection.<CompletionStage<Route>>empty(), (acc, bodyPart) -> {
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


}
