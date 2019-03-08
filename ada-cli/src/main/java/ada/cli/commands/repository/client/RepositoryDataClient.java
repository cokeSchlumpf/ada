package ada.cli.commands.repository.client;

import ada.cli.restclient.RestClient;
import com.ibm.ada.api.repository.RepositoryData;
import com.ibm.ada.model.Record;
import com.ibm.ada.model.RepositoryName;
import com.ibm.ada.model.Schema;
import com.ibm.ada.model.TransferResult;
import com.ibm.ada.model.auth.User;
import com.ibm.ada.model.versions.PatchVersion;
import com.ibm.ada.model.versions.Version;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public final class RepositoryDataClient implements RepositoryData {

    private final RestClient client;

    private final RepositoryName name;

    @Override
    public CompletionStage<TransferResult> append(User executor, Schema schema, Publisher<Record> data) {
        String uri = String.format("/api/v1/repositories/%s/data", name);
        return client.post(uri, TransferResult.class, data, Record.class).future();
    }

    @Override
    public CompletionStage<PatchVersion> commit(User executor, String message) {
        String uri = String.format("/api/v1/repositories/%s/data", name);
        return client.patch(uri, PatchVersion.class).future();
    }

    @Override
    public Publisher<Record> read(User executor, Version version) {
        String uri = String.format("/api/v1/repositories/%s/data/%s", name, version.versionString());
        return client.get(uri, Record.class).publisher();
    }

    @Override
    public CompletionStage<TransferResult> replace(User executor, Schema schema, Publisher<Record> data) {
        String uri = String.format("/api/v1/repositories/%s/data", name);
        return client.put(uri, TransferResult.class, data, Record.class).future();
    }

    @Override
    public CompletionStage<Optional<PatchVersion>> revert(User executor) {
        String uri = String.format("/api/v1/repositories/%s/data", name);
        return client
            .delete(uri, PatchVersion.class)
            .future()
            .thenApply(Optional::ofNullable);
    }

}
