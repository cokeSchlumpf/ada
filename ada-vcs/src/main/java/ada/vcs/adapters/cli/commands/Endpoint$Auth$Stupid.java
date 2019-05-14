package ada.vcs.adapters.cli.commands;

import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.adapters.cli.consoles.CommandLineConsole;
import ada.vcs.adapters.cli.exceptions.CommandNotInitializedException;
import ada.vcs.adapters.client.modifiers.AuthenticationMethod;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.List;
import java.util.Optional;

@CommandLine.Command(
    name = "stupid",
    description = "configures stupid authentication")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Endpoint$Auth$Stupid extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Endpoint$Auth auth;

    @CommandLine.Parameters(
        index = "0",
        paramLabel = "USER",
        description = "the username which should be used")
    private String user = null;

    @CommandLine.Parameters(
        index = "0",
        paramLabel = "ROLES",
        description = "the roles which should be assigned to the user")
    private List<String> roles = null;

    public static Endpoint$Auth$Stupid apply(CommandLineConsole console, CommandContext context) {
        return new Endpoint$Auth$Stupid(console, context, null, null, null);
    }

    @Override
    public void run() {
        getAuth().withEndpoint((home, endpoint) -> {
            AuthenticationMethod method = context.factories().authenticationMethodFactory().createStupid(user, roles);
            home.getConfiguration().addEndpoint(endpoint.withAuthenticationMethod(method));
            console.message("Updated authentication method of '%s'", endpoint.getAlias().getValue());
        });
    }

    public Endpoint$Auth getAuth() {
        return Optional.ofNullable(auth).orElseThrow(CommandNotInitializedException::apply);
    }

}
