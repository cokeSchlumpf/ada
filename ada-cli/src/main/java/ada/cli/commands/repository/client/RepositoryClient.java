package ada.cli.commands.repository.client;

import ada.cli.restclient.RestClient;
import com.ibm.ada.api.repository.Repository;
import com.ibm.ada.api.repository.RepositoryAdministration;
import com.ibm.ada.api.repository.RepositoryData;
import com.ibm.ada.model.RepositoryDetails;
import com.ibm.ada.model.RepositoryName;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public final class RepositoryClient implements Repository {

    private final RestClient client;

    private final RepositoryName name;

    private RepositoryDetails details;

    public static RepositoryClient apply(RestClient client, String name) {
        return apply(client, RepositoryName.apply(name), null);
    }

    public static RepositoryClient apply(RestClient client, RepositoryDetails details) {
        return apply(client, details.getName(), details);
    }

    @Override
    public RepositoryAdministration admin() {
        return RepositoryAdministrationClient.apply(client, name);
    }

    @Override
    public RepositoryData data() {
        return RepositoryDataClient.apply(client, name);
    }

    @Override
    public RepositoryDetails details() {
        if (details != null) {
            /*
             * If details has been passed into the constructor (e.g. after already receiving the whole details).
             * But then we delete the details as they might change on the server in the meantime.
             * This can be optimized here.
             */
            RepositoryDetails result = details;
            details = null;
            return result;
        } else {
            String uri = String.format("/api/v1/repositories/%s", name);

            return client
                .get(uri, RepositoryDetails.class)
                .await();
        }
    }

}
