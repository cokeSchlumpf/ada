package ada.server.repository.filesystem;

import com.ibm.ada.api.exceptions.RepositoryNotFoundException;
import com.ibm.ada.model.RepositoryName;
import com.ibm.ada.model.auth.User;
import com.ibm.ada.api.repository.Repositories;
import com.ibm.ada.api.repository.Repository;
import lombok.AllArgsConstructor;

import java.util.stream.Stream;

@AllArgsConstructor(staticName = "apply")
public class FSRepositories implements Repositories {

    @Override
    public Repository createRepository(User executor, RepositoryName name) {
        return null;
    }

    @Override
    public Repository getRepository(User executor, RepositoryName name) throws RepositoryNotFoundException {
        return null;
    }

    @Override
    public Stream<Repository> getRepositories(User executor) {
        return null;
    }

}
