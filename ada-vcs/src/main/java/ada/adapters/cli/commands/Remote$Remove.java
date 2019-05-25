package ada.adapters.cli.commands;

import ada.adapters.cli.commands.context.CommandContext;
import ada.adapters.cli.consoles.CommandLineConsole;
import ada.adapters.cli.exceptions.CommandNotInitializedException;
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
