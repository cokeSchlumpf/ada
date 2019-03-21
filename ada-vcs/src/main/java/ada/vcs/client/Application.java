package ada.vcs.client;

import ada.vcs.client.commands.CommandFactory;
import ada.vcs.client.commands.RootCommand;
import ada.vcs.client.consoles.CommandLineConsole;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;

import static picocli.CommandLine.Help.Ansi.AUTO;

@AllArgsConstructor(staticName = "apply")
public class Application {

    private final CommandFactory commandFactory;

    public static void main(String... args) {
        CommandLineConsole console = CommandLineConsole.apply();
        CommandFactory commands = CommandFactory.apply(console);
        Application runner = Application.apply(commands);

        runner.run(args);
    }

    public void run(String ...args) {
        final PrintStream ps = commandFactory.getConsole().printStream();
        final CommandLine cli = new CommandLine(new RootCommand(), commandFactory);

        try {
            cli.parseWithHandlers(
                new CommandLine.RunLast().useOut(ps).useErr(ps).useAnsi(AUTO),
                CommandLine.defaultExceptionHandler().useOut(ps).useErr(ps).useAnsi(AUTO),
                args);
        } catch (CommandLine.ExecutionException exception) {
            List<String> argsL = Lists.newArrayList(args);

            if (argsL.contains("-v") ||argsL.contains("--verbose")) {
                exception.printStackTrace(ps);
            }

            // TODO Default error handler with senseful message in case of exception from Ada.

            try {
                CommandLine.usage(exception.getCommandLine(), ps);
            } catch (Exception e) {
                e.printStackTrace(ps);
            }
        }
    }

}
