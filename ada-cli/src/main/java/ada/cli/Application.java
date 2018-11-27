package ada.cli;

import ada.cli.commands.AdaCommand;
import ada.client.output.Output;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private final ApplicationFactory factory;
    private final Output out;

    public Application(ApplicationFactory factory, Output out) {
        this.factory = factory;
        this.out = out;
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


    public static void main(String... args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setBannerMode(Banner.Mode.OFF);

        ConfigurableApplicationContext ctx = app.run(args);
        ctx.close();
    }


}
