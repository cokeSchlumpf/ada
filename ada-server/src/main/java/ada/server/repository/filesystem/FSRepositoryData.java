package ada.server.repository.filesystem;

import com.ibm.ada.api.repository.RepositoryData;
import com.ibm.ada.api.exceptions.LockedException;
import com.ibm.ada.api.exceptions.NotAuthorizedException;
import com.ibm.ada.api.exceptions.UncommittedChangesException;
import com.ibm.ada.model.repository.Record;
import com.ibm.ada.model.repository.Schema;
import com.ibm.ada.model.repository.TransferResult;
import com.ibm.ada.model.auth.User;
import com.ibm.ada.model.versions.PatchVersion;
import com.ibm.ada.model.versions.Version;
import org.reactivestreams.Publisher;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class FSRepositoryData implements RepositoryData {

    @Override
    public CompletionStage<TransferResult> append(User executor, Schema schema, Publisher<Record> data) throws LockedException, NotAuthorizedException, UncommittedChangesException {
        return null;
    }

    @Override
    public CompletionStage<PatchVersion> commit(User executor, String message) {
        return null;
    }

    @Override
    public Publisher<Record> read(User executor, Version version) {
        return null;
    }

    @Override
    public CompletionStage<TransferResult> replace(User executor, Schema schema, Publisher<Record> data) throws LockedException, NotAuthorizedException, UncommittedChangesException {
        return null;
    }

    @Override
    public CompletionStage<Optional<PatchVersion>> revert(User executor) throws LockedException, NotAuthorizedException {
        return null;
    }

}
