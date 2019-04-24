package ada.vcs.client.commands;

import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.List;

@AllArgsConstructor(staticName = "apply")
public final class CommandFactory implements CommandLine.IFactory {

    private final CommandLineConsole console;

    private final CommandContext context;

    public static CommandFactory apply(CommandLineConsole console) {
        return apply(console, CommandContext.apply());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K> K create(Class<K> cls) throws Exception {
        List<? extends StandardOptions> commands = Lists.newArrayList(
            Config.apply(console, context),
            Dataset.apply(console),
            Dataset$Extract.apply(console, context),
            Dataset$Targets.apply(console, context),
            Dataset$Targets$Add.apply(),
            Dataset$Targets$Add$Avro.apply(console, context),
            Dataset$Targets$Add$CSV.apply(console, context),
            Dataset$Targets$Add$Local.apply(console, context),
            Datasets.apply(console, context),
            Datasets$Add.apply(),
            Datasets$Add$CSV.apply(console, context),
            Datasets$Push.apply(console, context),
            Init.apply(console, context),
            Remotes.apply(console, context),
            Remotes$Add.apply(console, context));

        return (K) commands
            .stream()
            .filter(i -> i.getClass().equals(cls))
            .findFirst()
            .orElseThrow(() -> new Exception("Unknown class " + cls));
    }

    public CommandLineConsole getConsole() {
        return console;
    }

    public CommandContext getContext() {
        return context;
    }

}
