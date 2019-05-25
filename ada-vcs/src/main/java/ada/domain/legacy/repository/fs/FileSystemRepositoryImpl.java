package ada.domain.legacy.repository.fs;

import ada.commons.util.Operators;
import ada.commons.util.ResourceName;
import ada.domain.legacy.repository.api.version.Tag;
import ada.domain.legacy.repository.api.RefSpec;
import ada.domain.legacy.repository.api.RepositoryDirectAccess;
import ada.domain.legacy.repository.api.User;
import ada.domain.legacy.repository.api.exceptions.TagAlreadyExistsException;
import ada.domain.legacy.repository.api.exceptions.TagReferenceNotFoundException;
import ada.domain.legacy.repository.api.exceptions.VersionAlreadyExistsException;
import ada.domain.legacy.repository.api.exceptions.VersionReferenceNotFoundException;
import ada.domain.legacy.repository.api.version.VersionDetails;
import ada.domain.legacy.repository.api.version.VersionFactory;
import akka.NotUsed;
import akka.japi.Pair;
import akka.japi.function.Creator;
import akka.japi.function.Function;
import akka.stream.Materializer;
import akka.stream.alpakka.file.javadsl.LogRotatorSink;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor(staticName = "apply")
final class FileSystemRepositoryImpl implements RepositoryDirectAccess {

    private final FileSystemRepositorySettings settings;
    
    private final Path root;

    private final VersionFactory versionFactory;

    private final Materializer materializer;

    @Override
    public CompletionStage<RefSpec.TagRef> tag(User user, RefSpec.VersionRef ref, ResourceName alias) {
        Path source = root.resolve(ref.getId());

        if (!Files.exists(source) || !Files.isDirectory(source)) {
            return Operators.completeExceptionally(VersionReferenceNotFoundException.apply(ref));
        } else {
            return tags()
                .map(Tag::alias)
                .map(ResourceName::getValue)
                .filter(a -> a.equals(alias.getValue()))
                .runWith(Sink.seq(), materializer)
                .thenApply(existing -> {
                    if (!existing.isEmpty()) {
                        throw TagAlreadyExistsException.apply(alias);
                    } else {
                        return Operators.suppressExceptions(() -> {
                            Path detailsFile = source.resolve(settings.getDetailsFileName());
                            VersionDetails versionDetails;

                            try (InputStream is = Files.newInputStream(detailsFile)) {
                                versionDetails = versionFactory
                                    .createDetails(is)
                                    .withTag(versionFactory.createTag(alias, user));
                            }

                            try (OutputStream os = Files.newOutputStream(detailsFile)) {
                                versionDetails.writeTo(os);
                            }

                            return RefSpec.TagRef.apply(alias);
                        });
                    }
                });
        }
    }

    @Override
    public Source<Tag, NotUsed> tags() {
        return datasets()
            .map(VersionDetails::tag)
            .filter(Optional::isPresent)
            .map(Optional::get);
    }

    @Override
    public Source<VersionDetails, NotUsed> datasets() {
        return Operators.suppressExceptions(() -> {
            List<VersionDetails> details = Lists
                .newArrayList(Files
                    .newDirectoryStream(root)
                    .iterator())
                .stream()
                .map(version -> {
                    Path versionFile = version.resolve(settings.getDetailsFileName());

                    return Operators.suppressExceptions(() -> {
                        if (Files.exists(versionFile)) {
                            try (InputStream is = Files.newInputStream(versionFile)) {
                                return Optional.of(versionFactory.createDetails(is));
                            }
                        } else {
                            return Optional.<VersionDetails>empty();
                        }
                    });
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());


            return Source.from(details);
        });
    }

    @Override
    public Sink<GenericRecord, CompletionStage<VersionDetails>> push(VersionDetails details) {
        return Operators.suppressExceptions(() -> {
            final Schema schema = details.schema();
            final DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);

            Creator<Function<List<GenericRecord>, Iterable<ByteString>>> writeBytes = () -> {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
                dataFileWriter.create(schema, baos);
                dataFileWriter.flush();

                return records -> records
                    .stream()
                    .map(record -> {
                        Operators.suppressExceptions(() -> {
                            dataFileWriter.append(record);
                            dataFileWriter.flush();
                        });

                        ByteString result = ByteString.fromArray(baos.toByteArray());
                        baos.reset();

                        return result;
                    })
                    .collect(Collectors.toList());
            };


            return Flow
                .of(GenericRecord.class)
                .grouped(settings.getBatchSize())
                .statefulMapConcat(writeBytes)
                .via(Compression.gzip())
                .toMat(insert(details), Keep.right());
        });
    }

    @Override
    public Sink<ByteString, CompletionStage<VersionDetails>> insert(VersionDetails details) {
        return Operators.suppressExceptions(() -> {
            final Path target = root.resolve(details.id());
            final Path detailsFile = target.resolve(settings.getDetailsFileName());

            if (Files.exists(target)) {
                throw VersionAlreadyExistsException.apply(RefSpec.VersionRef.apply(details.id()));
            } else {
                Files.createDirectories(target);
            }

            try (OutputStream fos = Files.newOutputStream(detailsFile)) {
                details.writeTo(fos);
            }

            Creator<Function<ByteString, Optional<Path>>> rotationFunction = () -> {
                final long max = settings.getMaxFileSize().getBytes();
                final long[] size = new long[]{max};
                final int[] count = new int[]{0};

                return (element) -> {
                    if (size[0] + element.size() > max) {
                        String fileName = String.format(settings.getRecordsFileNameTemplate(), ++count[0]);
                        Path path = target.resolve(fileName);
                        size[0] = element.size();

                        Files.createFile(path);

                        return Optional.of(path);
                    } else {
                        size[0] += element.size();
                        return Optional.empty();
                    }
                };
            };

            return Flow.of(ByteString.class)
                .toMat(
                    LogRotatorSink.createFromFunction(rotationFunction),
                    Keep.right())
                .mapMaterializedValue(done -> done.thenApply(d -> details));
        });
    }

    @Override
    public Source<ByteString, CompletionStage<VersionDetails>> read(RefSpec refSpec) {
        CompletionStage<Source<ByteString, VersionDetails>> data = refSpec
            .map(
                tagRef -> datasets()
                    .filter(version -> version
                        .tag()
                        .map(tag -> tag.alias().getValue().equals(tagRef.getAlias().getValue()))
                        .orElse(false))
                    .runWith(Sink.seq(), materializer)
                    .thenApply(List::stream)
                    .thenApply(Stream::findFirst)
                    .thenApply(tag -> tag.orElseThrow(() -> TagReferenceNotFoundException.apply(tagRef)))
                    .thenApply(VersionDetails::id)
                    .thenApply(RefSpec.VersionRef::apply),
                CompletableFuture::completedFuture)
            .thenApply(versionRef -> {
                final Path source = root.resolve(versionRef.getId());
                final Path detailsFile = source.resolve(settings.getDetailsFileName());

                final VersionDetails versionDetails = Operators.suppressExceptions(() -> {
                    try (InputStream is = Files.newInputStream(detailsFile)) {
                        return versionFactory.createDetails(is);
                    }
                });

                if (!Files.isDirectory(source)) {
                    throw VersionReferenceNotFoundException.apply(versionRef);
                }

                Iterator<Path> pathsIt = Operators
                    .suppressExceptions(() -> Files.newDirectoryStream(source, path -> path.toString().endsWith("avro")))
                    .iterator();

                ArrayList<Path> paths = Lists.newArrayList(pathsIt);
                paths.sort(Comparator.naturalOrder());

                return Operators.suppressExceptions(() -> Source
                    .from(paths)
                    .flatMapConcat(FileIO::fromPath)
                    .mapMaterializedValue(done -> versionDetails));
            });

        return Source
            .fromSourceCompletionStage(data)
            .mapMaterializedValue(s -> s);
    }

    @Override
    public Source<GenericRecord, CompletionStage<VersionDetails>> pull(RefSpec refSpec) {
        final Pair<CompletionStage<VersionDetails>, InputStream> run = read(refSpec)
            .via(Compression.gunzip(1024))
            .toMat(StreamConverters.asInputStream(), Keep.both())
            .run(materializer);

        final CompletionStage<Source<GenericRecord, CompletionStage<VersionDetails>>> avro = run
            .first()
            .thenApply(versionDetails -> Operators.suppressExceptions(() -> {
                final DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(versionDetails.schema());
                final DataFileStream<GenericRecord> dataFileStream = new DataFileStream<>(run.second(), datumReader);

                return Source
                    .from(dataFileStream)
                    .watchTermination(Keep.right())
                    .mapMaterializedValue(done -> done.thenApply(ignore -> versionDetails));
            }));

        return Source.fromSourceCompletionStage(avro)
            .mapMaterializedValue(cs -> cs.thenCompose(vd -> vd));
    }

}
