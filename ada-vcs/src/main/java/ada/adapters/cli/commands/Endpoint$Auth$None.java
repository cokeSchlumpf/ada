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
    name = "none",
    description = "disables authentication for the endpoint")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Endpoint$Auth$None extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Endpoint$Auth auth;

    public static Endpoint$Auth$None apply(CommandLineConsole console, CommandContext context) {
        return new Endpoint$Auth$None(console, context, null);
    }

    @Override
    public void run() {
        getAuth().withEndpoint((home, endpoint) -> {
            AuthenticationMethod method = context.factories().authenticationMethodFactory().none();
            home.getConfiguration().addEndpoint(endpoint.withAuthenticationMethod(method));
            console.message("Updated authentication method of '%s'", endpoint.getAlias().getValue());
        });
    }

    public Endpoint$Auth getAuth() {
        return Optional.ofNullable(auth).orElseThrow(CommandNotInitializedException::apply);
    }

}
