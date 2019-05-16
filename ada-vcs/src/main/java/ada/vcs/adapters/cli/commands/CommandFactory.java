package ada.vcs.adapters.cli.commands;

import ada.vcs.adapters.cli.commands.context.CommandContext;
import ada.vcs.adapters.cli.consoles.CommandLineConsole;
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
            Dataset$Print.apply(console, context),
            Dataset$Remove.apply(console, context),
            Dataset$Rename.apply(console, context),
            Dataset$Target.apply(console, context),
            Dataset$Target$Remove.apply(console, context),
            Dataset$Target$Rename.apply(console, context),
            Dataset$Targets.apply(console, context),
            Dataset$Targets$Add.apply(),
            Dataset$Targets$Add$Avro.apply(console, context),
            Dataset$Targets$Add$CSV.apply(console, context),
            Datasets.apply(console, context),
            Datasets$Add.apply(),
            Datasets$Add$CSV.apply(console, context),
            Datasets$Extract.apply(console, context),
            Datasets$Push.apply(console, context),
            Endpoint.apply(console, context),
            Endpoint$Auth.apply(console, context),
            Endpoint$Auth$API.apply(console, context),
            Endpoint$Auth$None.apply(console, context),
            Endpoint$Auth$Stupid.apply(console, context),
            Endpoint$Remove.apply(console, context),
            Endpoint$Rename.apply(console, context),
            Endpoints.apply(console, context),
            Endpoints$Add.apply(console, context),
            Init.apply(console, context),
            Remote.apply(console, context),
            Remote$Rename.apply(console, context),
            Remote$Remove.apply(console, context),
            Remotes.apply(console, context),
            Remotes$Add.apply(console, context),
            Repositories.apply(console, context),
            Repositories$Create.apply(console, context),
            Repository.apply(console, context),
            Repository$Grant.apply(console, context),
            Repository$Grants.apply(console),
            Repository$Revoke.apply(console, context),
            Repository$Schema.apply(console, context),
            Server.apply(context));

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
