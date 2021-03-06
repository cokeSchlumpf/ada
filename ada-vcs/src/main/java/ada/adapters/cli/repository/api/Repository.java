package ada.adapters.cli.repository.api;

import ada.commons.util.ResourceName;
import ada.domain.dvc.values.repository.RefSpec;
import ada.domain.dvc.values.repository.User;
import ada.domain.dvc.values.repository.version.Tag;
import ada.domain.dvc.values.repository.version.VersionDetails;
import akka.NotUsed;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.apache.avro.generic.GenericRecord;

import java.util.concurrent.CompletionStage;

public interface Repository {

    CompletionStage<RefSpec.TagRef> tag(User user, RefSpec.VersionRef ref, ResourceName name);

    Source<Tag, NotUsed> tags();

    Source<VersionDetails, NotUsed> datasets();

    Sink<GenericRecord, CompletionStage<VersionDetails>> push(VersionDetails version);

    Source<GenericRecord, CompletionStage<VersionDetails>> pull(RefSpec refSpec);

}
