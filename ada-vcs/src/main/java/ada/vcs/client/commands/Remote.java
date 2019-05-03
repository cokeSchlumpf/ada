package ada.vcs.client.commands;

import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

@CommandLine.Command(
    name = "remote",
    description = "work with a specific remote",
    subcommands = {
        Remote$Remove.class,
        Remote$Rename.class
    })
@AllArgsConstructor(staticName = "apply")
public final class Remote extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.Parameters(index = "0", description = "The alias of the remote")
    private String alias;

    public static Remote apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null);
    }

    @Override
    public void run() {
        context.withProject(project -> {
            ada.vcs.client.core.remotes.Remote remote = project.getRemote(alias);
            String upstreamInfo = project.getUpstream().filter(r -> r.alias().getValue().equals(alias)).map(i -> "* ").orElse("  ");
            console.message("%s%s %s", upstreamInfo, alias, remote.info());
        });
    }

    public String alias() {
        return alias;
    }

}
