package ada.server.repository.secured;

import com.ibm.ada.api.repository.RepositoryAdministration;
import com.ibm.ada.api.exceptions.NotAuthorizedException;
import com.ibm.ada.model.auth.AuthorizationRequest;
import com.ibm.ada.model.repository.RepositoryDetails;
import com.ibm.ada.model.auth.User;

import java.util.concurrent.CompletionStage;

public class SecuredRepositoryAdministration implements RepositoryAdministration {

    @Override
    public CompletionStage<RepositoryDetails> changeOwner(User executor, AuthorizationRequest auth) throws NotAuthorizedException {
        return null;
    }

    @Override
    public CompletionStage<Void> delete(User executor) throws NotAuthorizedException {
        return null;
    }

    @Override
    public CompletionStage<RepositoryDetails> grant(User executor, AuthorizationRequest auth) throws NotAuthorizedException {
        return null;
    }

    @Override
    public CompletionStage<RepositoryDetails> revoke(User executor, AuthorizationRequest auth) throws NotAuthorizedException {
        return null;
    }

}
