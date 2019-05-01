package ada.vcs.client.commands;

import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
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
                Dataset$Extract
                    .apply(console, context, dsCmd, Lists.newArrayList(), false)
                    .run();
            }));
    }

}
