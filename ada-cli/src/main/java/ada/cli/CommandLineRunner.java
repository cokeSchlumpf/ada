package ada.cli;

import ada.cli.commands.AdaCommand;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Value;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;

import static picocli.CommandLine.Help.Ansi.AUTO;

@Value
@AllArgsConstructor(staticName = "apply")
public class CommandLineRunner {

    private final CommandFactory commandFactory;

    public void run(String ...args) {
        final PrintStream ps = commandFactory.console().printStream();
        final CommandLine cli = new CommandLine(new AdaCommand(), commandFactory);

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
