package ada.vcs.adapters.cli.commands;

import ada.commons.util.ResourceName;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.adapters.cli.consoles.CommandLineConsole;
import ada.vcs.adapters.cli.exceptions.CommandNotInitializedException;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.Optional;

@CommandLine.Command(
    name = "rename",
    description = "rename an existing remote")
@AllArgsConstructor(staticName = "apply")
public final class Remote$Rename extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Remote remote;

    @CommandLine.Parameters(
        index = "0",
        paramLabel = "ALIAS",
        description = "the new alias for the remote")
    private String alias = null;

    public static Remote$Rename apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null, "");
    }

    @Override
    public void run() {
        context.withProject(project -> {
            final Remote remote = getRemote().orElseThrow(CommandNotInitializedException::apply);
            ada.vcs.adapters.cli.core.remotes.Remote current = project.getRemote(remote.alias());
            Optional<String> upstream = project
                .getUpstream()
                .map(r -> r.alias().getValue())
                .filter(a -> a.equals(remote.alias()));

            ResourceName alias$new = ResourceName.apply(alias);
            project.addRemote(current.withAlias(alias$new));
            project.removeRemote(remote.alias());
            upstream.ifPresent(i -> project.updateUpstream(alias$new.getValue()));

            console.message("Renamed remote '%s' to '%s'.", remote.alias(), alias);
        });
    }

    public Optional<Remote> getRemote() {
        return Optional.ofNullable(remote);
    }

}
