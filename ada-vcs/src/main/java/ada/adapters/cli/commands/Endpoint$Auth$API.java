package ada.adapters.cli.commands;

import ada.adapters.cli.consoles.CommandLineConsole;
import ada.adapters.cli.exceptions.CommandNotInitializedException;
import ada.adapters.client.modifiers.AuthenticationMethod;
import ada.adapters.cli.commands.context.CommandContext;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.Optional;

@CommandLine.Command(
    name = "api",
    description = "configures API authentication")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Endpoint$Auth$API extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Endpoint$Auth auth;

    @CommandLine.Parameters(
        index = "0",
        paramLabel = "KEY",
        description = "the API client-id/ -key")
    private String key = null;

    @CommandLine.Parameters(
        index = "0",
        paramLabel = "SECRET",
        description = "the API client-secret")
    private String secret = null;

    public static Endpoint$Auth$API apply(CommandLineConsole console, CommandContext context) {
        return new Endpoint$Auth$API(console, context, null, null, null);
    }

    @Override
    public void run() {
        getAuth().withEndpoint((home, endpoint) -> {
            AuthenticationMethod method = context.factories().authenticationMethodFactory().createAPIKey(key, secret);
            home.getConfiguration().addEndpoint(endpoint.withAuthenticationMethod(method));
            console.message("Updated authentication method of '%s'", endpoint.getAlias().getValue());
        });
    }

    public Endpoint$Auth getAuth() {
        return Optional.ofNullable(auth).orElseThrow(CommandNotInitializedException::apply);
    }

}
