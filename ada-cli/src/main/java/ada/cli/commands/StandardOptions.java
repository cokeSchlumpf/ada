package ada.cli.commands;

import picocli.CommandLine;

public class StandardOptions {

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display help for command")
    boolean help;

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Verbose output including errors and traces")
    boolean debug;

}
