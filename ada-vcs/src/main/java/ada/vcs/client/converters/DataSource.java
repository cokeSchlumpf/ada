package ada.vcs.client.converters;

import akka.Done;

import java.util.concurrent.CompletionStage;

public interface DataSource {

    CompletionStage<Done> get(boolean incremental);

}
