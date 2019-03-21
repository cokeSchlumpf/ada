package ada.cli.commands.repository.commands;

import ada.cli.commands.StandardOptions;
import picocli.CommandLine;

@CommandLine.Command(
    name = "init",
    description = "initializes the repository in the current directory",
    subcommands = {
        InitMetaCommand.class,
        InitCSVCommand.class
    })
public class InitCommand extends StandardOptions {

    @CommandLine.ParentCommand
    private RepositoryCommand repositoryCommand;

    private InitCommand() {

    }

    public static InitCommand apply() {
        return new InitCommand();
    }

    public RepositoryCommand getRepositoryCommand() {
        return repositoryCommand;
    }

}
