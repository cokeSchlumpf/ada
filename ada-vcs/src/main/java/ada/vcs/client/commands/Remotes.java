package ada.vcs.client.commands;

import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.core.project.AdaProject;
import ada.vcs.client.core.remotes.Remote;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CommandLine.Command(
    name = "remotes",
    description = "list or add remotes",
    subcommands = {
        Remotes$Add.class
    })
@AllArgsConstructor(staticName = "apply")
public final class Remotes extends StandardOptions implements ProjectCommand {

    private CommandLineConsole console;

    @Override
    public void run(AdaProject project) {
        List<Remote> remotes = project.getRemotes().collect(Collectors.toList());
        Optional<Remote> upstream = project.getUpstream();


        if (remotes.size() > 0) {
            console.table(
                Lists.newArrayList("Alias", "Type"),
                remotes
                    .stream()
                    .sorted()
                    .map(remote -> Lists.newArrayList(
                        upstream
                            .filter(u -> u.alias().getValue().equals(remote.alias().getValue()))
                            .map(i -> "* ")
                            .orElse("  ") + remote.alias().getValue(),
                        remote.info()))
                    .collect(Collectors.toList()),
                false);
        } else {
            console.message("No remotes in project.");
        }
    }
}
