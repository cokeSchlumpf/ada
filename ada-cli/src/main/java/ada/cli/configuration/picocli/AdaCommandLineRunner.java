package ada.cli.configuration.picocli;

import ada.cli.Application;
import ada.cli.commands.AdaCommand;
import ada.client.output.Output;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@Component
@ConditionalOnMissingBean(name = "adaTestCommandLineRunner")
public class AdaCommandLineRunner implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private final ApplicationFactory factory;
    private final Output out;

    public AdaCommandLineRunner(ApplicationFactory factory, Output out) {
        this.factory = factory;
        this.out = out;
    }

    protected void executeCommand(String... args) throws CommandLine.ExecutionException {
        final CommandLine cli = new CommandLine(new AdaCommand(), factory);

        cli.parseWithHandlers(
            new CommandLine.RunLast().useOut(out.getStream()).useAnsi(CommandLine.Help.Ansi.ON),
            CommandLine.defaultExceptionHandler().useErr(out.getStream()).useAnsi(CommandLine.Help.Ansi.OFF),
            args);
    }

    @Override
    public void run(String... args) {
        try {
            final CommandLine cli = new CommandLine(new AdaCommand(), factory);
            cli.parseWithHandler(new CommandLine.RunLast(), args);
        } catch (CommandLine.ExecutionException exception) {
            LOG.error(
                String.format(
                    "An exception occurred while executing Ada with arguments: %s",
                    Strings.join(Lists.newArrayList(args), ',')),
                exception);

            try {
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                final PrintStream ps = new PrintStream(outputStream, true, StandardCharsets.UTF_8.toString());

                CommandLine.usage(exception.getCommandLine(), ps);
                out.message(outputStream.toString(StandardCharsets.UTF_8.toString()));
            } catch (Exception e) {
                LOG.error("Exception occurred while printing help output", e);
            }
        }
    }

}
