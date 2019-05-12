package ada.vcs.client.commands;

import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.exceptions.CommandNotInitializedException;
import ada.vcs.server.adapters.client.modifiers.AuthenticationMethod;
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
