package ada.vcs.client.commands.context;

import ada.vcs.client.converters.api.DataSinkFactory;
import ada.vcs.client.converters.api.DataSourceFactory;
import ada.vcs.client.core.dataset.DatasetFactory;
import ada.vcs.client.core.project.AdaProjectFactory;
import ada.vcs.client.core.remotes.RemotesFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public final class Factories {

    private final ObjectMapper om;

    public DatasetFactory datasetFactory() {
        return DatasetFactory.apply(om, DataSourceFactory.apply(), DataSinkFactory.apply());
    }

    public AdaProjectFactory projectFactory() {
        return AdaProjectFactory.apply(remotesFactory(), datasetFactory());
    }

    public RemotesFactory remotesFactory() {
        return RemotesFactory.apply(om);
    }

}
