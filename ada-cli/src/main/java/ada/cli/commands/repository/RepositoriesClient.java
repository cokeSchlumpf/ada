package ada.cli.commands.repository;

import com.ibm.ada.api.repository.Repository;
import com.ibm.ada.api.repository.RepositoryAdministration;
import com.ibm.ada.api.repository.RepositoryData;
import com.ibm.ada.api.model.RepositoryDetails;

public class RepositoriesClient implements Repository {

    @Override
    public RepositoryAdministration admin() {
        return null;
    }

    @Override
    public RepositoryData data() {
        return null;
    }

    @Override
    public RepositoryDetails details() {
        return null;
    }

}