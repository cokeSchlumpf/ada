package ada.vcs.client.core.repository.fs;

import ada.commons.databind.ObjectMapperFactory;
import ada.commons.util.ResourceName;
import ada.vcs.client.core.repository.api.RefSpec;
import ada.vcs.client.core.repository.api.User;
import ada.vcs.client.core.repository.api.version.VersionDetails;
import ada.vcs.client.core.repository.api.version.VersionFactory;
import ada.vcs.client.util.AbstractAdaTest;
import ada.vcs.client.util.TestDataFactory;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.apache.avro.generic.GenericRecord;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class FileSystemRepositoryImplUTest extends AbstractAdaTest {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        final User user = User.FakeImpl.apply();
        final TestDataFactory data = TestDataFactory.apply();
        final VersionFactory versionFactory = VersionFactory.apply(ObjectMapperFactory.apply().create(true));

        final FileSystemRepositoryImpl repository = FileSystemRepositoryImpl.apply(
            FileSystemRepositorySettings
                .Builder
                .apply(getDirectory(), ObjectMapperFactory.apply().create(true))
                .build(),
            versionFactory,
            getContext().materializer());

        final List<GenericRecord> samples = data.getRecordsAsStream().collect(Collectors.toList());

        {
            /*
             * When the repository is new, the list of tags should be empty,
             * as well as the history (list of versions).
             */
            assertThat(
                repository
                    .tags()
                    .runWith(Sink.seq(), getContext().materializer())
                    .toCompletableFuture()
                    .get()
            ).isEmpty();

            assertThat(
                repository
                    .history()
                    .runWith(Sink.seq(), getContext().materializer())
                    .toCompletableFuture()
                    .get()
            ).isEmpty();
        }

        String id;

        {
            /*
             * When pushing data into the repository a new version is created.
             */
            VersionDetails details = Source
                .from(samples)
                .runWith(
                    repository.push(data.getSchema(), user),
                    getContext().materializer())
                .toCompletableFuture()
                .get();

            assertThat(details.tag().isPresent()).isFalse();
            assertThat(details.schema()).isEqualTo(data.getSchema());
            assertThat(details.user()).isEqualTo(user);

            List<VersionDetails> history = repository
                .history()
                .runWith(Sink.seq(), getContext().materializer())
                .toCompletableFuture()
                .get();

            assertThat(history)
                .hasSize(1)
                .contains(details);

            id = details.id();
        }

        {
            List<GenericRecord> output = repository
                .pull(RefSpec.fromId(id))
                .runWith(Sink.seq(), getContext().materializer())
                .toCompletableFuture()
                .get();

            assertThat(output)
                .hasSize(samples.size())
                .contains(samples.get(0))
                .contains(samples.get(samples.size() - 1));
        }

        {
            RefSpec.TagRef tagRef = repository
                .tag(user, RefSpec.fromId(id), ResourceName.apply("my-tag"))
                .toCompletableFuture()
                .get();

            assertThat(tagRef.toString()).isEqualTo("tags/my-tag");

            System.out.println(repository
                .history()
                .runWith(Sink.seq(), getContext().materializer())
                .toCompletableFuture()
                .get());

            assertThat(
                repository
                    .tags()
                    .runWith(Sink.seq(), getContext().materializer())
                    .toCompletableFuture()
                    .get())
                .hasSize(1);

            List<GenericRecord> output = repository
                .pull(tagRef)
                .runWith(Sink.seq(), getContext().materializer())
                .toCompletableFuture()
                .get();

            assertThat(output)
                .hasSize(samples.size())
                .contains(samples.get(0))
                .contains(samples.get(samples.size() - 1));
        }
    }

}
