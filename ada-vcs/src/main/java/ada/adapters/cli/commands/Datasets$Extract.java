package ada.adapters.cli.commands;

import ada.adapters.cli.consoles.CommandLineConsole;
import ada.adapters.cli.commands.context.CommandContext;
import ada.commons.exceptions.AdaException;
import com.google.common.collect.Lists;
import picocli.CommandLine;

@CommandLine.Command(
    name = "extract",
    description = "extracts all datasets")
public final class Datasets$Extract extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    private Datasets$Extract(CommandLineConsole console, CommandContext context) {
        this.console = console;
        this.context = context;
    }

    public static Datasets$Extract apply(CommandLineConsole console, CommandContext context) {
        return new Datasets$Extract(console, context);
    }

    @Override
    public void run() {
        context.withProject(project -> project
            .getDatasets()
            .forEach(dataset -> {
                Dataset dsCmd = Dataset.apply(console, dataset.alias().getValue());

                try {
                    Dataset$Extract
                        .apply(console, context, dsCmd, Lists.newArrayList(), false)
                        .run();
                } catch (RuntimeException exception) {
                    if (exception instanceof AdaException) {
                        console.message(exception.getMessage());
                    } else {
                        throw exception;
                    }
                }
            }));
    }

}
