package ada.vcs.client.commands;

import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.core.AdaHome;
import ada.vcs.client.core.configuration.AdaConfiguration;
import ada.vcs.client.core.project.AdaProject;
import ada.vcs.client.exceptions.ExitWithErrorException;
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

    @CommandLine.Option(
        names = {"--global"},
        description = "show or set global variable value")
    private boolean global;

    @CommandLine.Option(
        names = {"--unset"},
        description = "removes a value from configuration"
    )
    private boolean unset;

    public static Config apply(CommandLineConsole console, CommandContext context) {
        return apply(console, context, null, null, false, false);
    }

    @Override
    public void run() {
        if (alias == null && value == null) {
            show();
        } else if (alias != null && value == null) {
            if (unset) {
                unsetValue(alias);
            } else {
                showValue(alias);
            }
        } else if (alias != null){
            setValue(alias, value);
        } else {
            show();
        }
    }

    private void show() {
        List<Pair<String, Object>> values = Lists.newArrayList();
        values.add(Pair.of("user", getUser()));
        console.table(values);
    }

    private void showValue(String key) {
        if (key.equals("user")) {
            console.message(getUser());
        } else {
            throw ExitWithErrorException.apply("Unknown configuration key");
        }
    }

    private void setValue(String key, String value) {
        if (key.equals("user")) {
            setUser(value);
            console.message("Updated '%s' to '%s'", key, value);
        } else {
            throw ExitWithErrorException.apply("Unknown configuration key");
        }
    }

    private void unsetValue(String key) {
        if (key.equals("user")) {
            getConfig().unsetUser();
        }
    }

    private AdaConfiguration getConfig() {
        if (global) {
            return context.fromAdaHome(AdaHome::getConfiguration);
        } else {
            return context.fromProject(AdaProject::getConfiguration);
        }
    }

    private String getUser() {
        return getConfig()
            .getUser()
            .map(User::toString)
            .orElse("<not set>");
    }

    private void setUser(String value) {
        getConfig().setUser(User.fromString(value));
    }

}
