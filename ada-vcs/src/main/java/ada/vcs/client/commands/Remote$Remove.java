package ada.vcs.client.commands;

import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.exceptions.CommandNotInitializedException;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.Optional;

@CommandLine.Command(
    name = "remove",
    description = "removes a remote from the project")
@AllArgsConstructor(staticName = "apply")
public final class Remote$Remove extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Remote remote;

    public static Remote$Remove apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null);
    }

    @Override
    public void run() {
        context.withProject(project -> {
            final Remote remote = getRemote().orElseThrow(CommandNotInitializedException::apply);
            project.removeRemote(remote.alias());
            console.message("Removed remote '%s' from project. Be aware that pushed references might still exist.");
        });
    }

    public Optional<Remote> getRemote() {
        return Optional.ofNullable(remote);
    }

}
