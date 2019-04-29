package ada.vcs.server.directives;

import ada.commons.util.Operators;
import ada.vcs.client.core.Writable;
import ada.vcs.client.core.repository.api.Repository;
import ada.vcs.client.core.repository.api.version.VersionDetails;
import ada.vcs.client.core.repository.api.version.VersionFactory;
import ada.vcs.client.core.repository.fs.FileSystemRepositoryFactory;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.Multipart;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.japi.function.Function;
import akka.japi.function.Function2;
import akka.stream.javadsl.Compression;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.StreamConverters;
import akka.util.ByteString;
import lombok.AllArgsConstructor;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;

import java.io.InputStream;
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

    public Route repository(Function<Repository, Route> next) {
        return path(alias -> {
            Repository repository = repositoryFactory.create(repositoryRoot.resolve(alias));

            try {
                return next.apply(repository);
            } catch (Exception e) {
                e.printStackTrace();
                return complete(StatusCodes.INTERNAL_SERVER_ERROR, "Something went wrong ...");
            }
        });
    }

    @Override
    public Route records(Function2<VersionDetails, Source<GenericRecord, CompletionStage<VersionDetails>>, CompletionStage<Route>> next) {
        return extractMaterializer(materializer ->
                entity(Unmarshaller.entityToMultipartFormData(), formData -> {
                    CompletionStage<RecordsUploadDataCollection> result = formData
                        .getParts()
                        .map(i -> ((Multipart.FormData.BodyPart) i))
                        .runFoldAsync(RecordsUploadDataCollection.empty(), (acc, bodyPart) -> {
                            switch (bodyPart.getName()) {
                                case "records":
                                    return acc
                                        .process(details -> {
                                            final InputStream is = bodyPart.getEntity().getDataBytes()
                                                .via(Compression.gunzip(1024))
                                                .runWith(StreamConverters.asInputStream(), materializer);

                                            return CompletableFuture
                                                .supplyAsync(() -> Operators.suppressExceptions(() -> {
                                                    final DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(details.schema());
                                                    final DataFileStream<GenericRecord> dataFileStream = new DataFileStream<>(is, datumReader);

                                                    return Source
                                                        .from(dataFileStream)
                                                        .watchTermination(Keep.right())
                                                        .mapMaterializedValue(done -> done.thenApply(ignore -> details));
                                                }))
                                                .thenCompose(source -> Operators.suppressExceptions(() -> next.apply(details, source)));
                                        });

                                case "details":
                                    return bodyPart
                                        .getEntity()
                                        .getDataBytes()
                                        .runFold(ByteString.empty(), ByteString::concat, materializer)
                                        .toCompletableFuture()
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
                        .route()
                        .orElseGet(() -> complete(StatusCodes.BAD_REQUEST, "Wrong request format")));
                })
        );
    }


}
