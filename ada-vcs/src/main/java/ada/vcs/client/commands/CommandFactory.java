package ada.vcs.client.commands;

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
            Dataset.apply(console),
            Dataset$Extract.apply(console, context),
            Dataset$Targets.apply(console),
            Dataset$Targets$Add.apply(),
            Dataset$Targets$Add$Avro.apply(console),
            Dataset$Targets$Add$CSV.apply(console),
            Dataset$Targets$Add$Local.apply(console),
            Datasets.apply(console),
            Datasets$Add.apply(),
            Datasets$Add$CSV.apply(console, context),
            Init.apply(console),
            Remotes.apply(console),
            Remotes$Add.apply(console));

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
