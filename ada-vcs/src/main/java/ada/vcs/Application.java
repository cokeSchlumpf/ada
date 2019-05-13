package ada.vcs;

import ada.commons.util.Operators;
import ada.vcs.client.commands.CommandFactory;
import ada.vcs.client.commands.Root;
import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.commons.exceptions.AdaException;
import ada.vcs.client.exceptions.ExitWithErrorException;
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

            System.exit(runner.run(args));
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

            CommandLineConsole console = CommandLineConsole.apply();
            CommandContext context = CommandContext.apply();
            CommandFactory commands = CommandFactory.apply(console, context);
            Application runner = Application.apply(commands);

            runner.run(command.split(" "));
        }
    }

    public int run(String... args) {
        int[] exitCode = new int[]{0};

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
            Optional<AdaException> adaException = Operators.hasCause(exception, AdaException.class);

            if (adaException.isPresent()) {
                AdaException ex = adaException.get();

                commandFactory.getConsole().message(ex.getMessage());

                if (ex instanceof ExitWithErrorException) {
                    exitCode[0] = ((ExitWithErrorException) ex).getExitCode();
                }
            } else {
                commandFactory
                    .getConsole()
                    .message("¯\\_(ツ)_/¯ Ups... something went wrong...\n" +
                        "           " + Operators.extractMessage(exception));
            }

            if (argsL.contains("-v") || argsL.contains("--verbose")) {
                exception.printStackTrace(ps);
            }
        } finally {
            commandFactory.getContext().shutdown();
            timer.ifPresent(sw -> {
                sw.stop();
                commandFactory.getConsole().message(sw.toString());
            });
        }

        return exitCode[0];
    }

}
