package ada.vcs.client;

import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.commands.CommandFactory;
import ada.vcs.client.commands.Root;
import ada.vcs.client.consoles.CommandLineConsole;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static picocli.CommandLine.Help.Ansi.AUTO;

@AllArgsConstructor(staticName = "apply")
public class Application {

    private final CommandFactory commandFactory;

    public static void main(String... args) {
        if (args.length == 0) {
            repl();
        } else {
            CommandLineConsole console = CommandLineConsole.apply();
            CommandContext context = CommandContext.apply();
            CommandFactory commands = CommandFactory.apply(console, context);
            Application runner = Application.apply(commands);

            runner.run(args);
        }
    }

    public static void repl() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String command = scanner.nextLine();

            if (command.equals("exit") || command.equals("quit") || command.equals("")) {
                break;
            }

            main(command.split(" "));
        }
    }

    public void run(String ...args) {
        List<String> argsL = Lists.newArrayList(args);
        Optional<Stopwatch> timer = Optional.empty();

        if (argsL.contains("--time")) {
            timer = Optional.of(Stopwatch.createStarted());
        }

        final PrintStream ps = commandFactory.getConsole().printStream();
        final CommandLine cli = new CommandLine(new Root(), commandFactory);

        try {
            cli.parseWithHandlers(
                new CommandLine.RunLast().useOut(ps).useErr(ps).useAnsi(AUTO),
                CommandLine.defaultExceptionHandler().useOut(ps).useErr(ps).useAnsi(AUTO),
                args);
        } catch (CommandLine.ExecutionException exception) {


            if (argsL.contains("-v") ||argsL.contains("--verbose")) {
                exception.printStackTrace(ps);
            }

            // TODO Default error handler with senseful message in case of exception from Ada.

            try {
                CommandLine.usage(exception.getCommandLine(), ps);
            } catch (Exception e) {
                e.printStackTrace(ps);
            }
        } finally {
            commandFactory.getContext().shutdown();
            timer.ifPresent(sw -> {
                sw.stop();
                commandFactory.getConsole().message(sw.toString());
            });
        }
    }

}
