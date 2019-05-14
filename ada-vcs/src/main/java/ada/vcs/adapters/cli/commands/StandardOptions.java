package ada.vcs.adapters.cli.commands;

import picocli.CommandLine;

public abstract class StandardOptions {

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display help for command")
    boolean help;

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Verbose output including errors and traces")
    boolean debug;

    @CommandLine.Option(names = {"--time"}, description = "Displays time to execute command")
    boolean time;

}
