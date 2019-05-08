package ada.vcs.client.commands;

import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.shared.repository.api.User;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(
    name = "config",
    description = "show or set configuration")
@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public final class Config extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.Parameters(
        index = "0",
        arity = "0..1",
        description = "the alias of the config value")
    private String alias;

    @CommandLine.Parameters(
        index = "0",
        arity = "0..1",
        description = "a value to set the config value")
    private String value;

    // TODO: Global?

    public static Config apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null, null);
    }

    @Override
    public void run() {
        if (alias == null && value == null) {
            show();
        } else if (alias != null && value == null) {
            showValue(alias);
        } else {
            setValue(alias, value);
        }
    }

    private void show() {
        List<Pair<String, Object>> values = Lists.newArrayList();

        context.withProject(project -> {
            values.add(Pair.of("user", project
                .getConfiguration()
                .getUser()
                .map(User::toString)
                .orElseGet(() -> "<not set>")));

            console.table(values);
        });
    }

    private void showValue(String key) {
        console.message("Not implemented yet.");
    }

    private void setValue(String key, String value) {
        console.message("Not implemented yet.");
    }

}
