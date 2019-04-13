package ada.vcs.client.commands;

import ada.vcs.client.core.project.AdaProject;
import ada.vcs.client.core.project.AdaProjectFactory;
import ada.vcs.client.exceptions.NoProjectException;

public interface ProjectCommand extends Runnable {

    @Override
    default void run() {
        AdaProject project = AdaProjectFactory
            .apply()
            .fromHere()
            .orElseThrow(NoProjectException::apply);

        run(project);
    }

    void run(AdaProject project);

}
