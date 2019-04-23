package ada.vcs.client.core.repository.api;

import ada.commons.util.ResourceName;
import ada.vcs.client.core.repository.api.version.Tag;
import ada.vcs.client.core.repository.api.version.VersionDetails;
import akka.NotUsed;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import java.util.concurrent.CompletionStage;

public interface Repository {

    CompletionStage<RefSpec.TagRef> tag(User user, RefSpec.VersionRef ref, ResourceName name);

    Source<Tag, NotUsed> tags();

    Source<VersionDetails, NotUsed> history();

    Sink<GenericRecord, CompletionStage<VersionDetails>> push(Schema schema, User user, String message);

    Source<GenericRecord, CompletionStage<VersionDetails>> pull(RefSpec refSpec);

}
