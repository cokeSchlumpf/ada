package ada.vcs.adapters.cli.commands;

import ada.commons.util.ResourceName;
import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.adapters.cli.consoles.CommandLineConsole;
import ada.vcs.adapters.cli.core.AdaHome;
import ada.vcs.adapters.cli.core.configuration.AdaConfiguration;
import ada.vcs.adapters.cli.core.project.AdaProject;
import ada.vcs.adapters.cli.exceptions.ExitWithErrorException;
import ada.vcs.domain.legacy.repository.api.User;
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
        switch (key) {
            case "user":
                console.message(getUser());
                break;
            case "namespace":
                console.message(getNamespace());
                break;
            default:
                throw ExitWithErrorException.apply("Unknown configuration key");
        }
    }

    private void setValue(String key, String value) {
        switch (key) {
            case "user":
                setUser(value);
                break;
            case "namespace":
                setNamespace(value);
                break;
            default:
                throw ExitWithErrorException.apply("Unknown configuration key");
        }

        console.message("Updated '%s' to '%s'", key, value);
    }

    private void unsetValue(String key) {
        switch (key) {
            case "user":
                getConfig().unsetUser();
                break;
            case "namespace":
                getConfig().unsetNamespace();
                break;
            default:
                throw ExitWithErrorException.apply("Unknown configuration key");
        }

        console.message("Unset configuration value for '%s'", key);
    }

    private AdaConfiguration getConfig() {
        if (global) {
            return context.fromAdaHome(AdaHome::getConfiguration);
        } else {
            return context.fromProject(AdaProject::getConfiguration);
        }
    }

    private String getNamespace() {
        return getConfig()
            .getNamespace()
            .map(ResourceName::getValue)
            .orElse("<not set>");
    }

    private String getUser() {
        return getConfig()
            .getUser()
            .map(User::toString)
            .orElse("<not set>");
    }

    private void setNamespace(String value) {
        getConfig().setNamespace(ResourceName.apply(value));
    }

    private void setUser(String value) {
        getConfig().setUser(User.fromString(value));
    }

}
