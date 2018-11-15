package ada.client;

import ada.client.commands.AboutCommand;
import ada.client.commands.AdaCommand;
import ada.client.output.CliOutput;
import ada.client.output.DefaultCliOutput;
import ada.web.controllers.AboutResource;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import picocli.CommandLine;

public final class CliApplicationBuilder {

    private final CommandLine cli;

    private final Materializer mat;

    private CliOutput out;

    private CliApplicationBuilder(Materializer mat, CommandLine cli, CliOutput out) {
        this.cli = cli;
        this.mat = mat;
        this.out = out;
    }

    public static CliApplicationBuilder apply(Materializer mat) {
        CommandLine commandLine = new CommandLine(new AdaCommand());
        return new CliApplicationBuilder(mat, commandLine, DefaultCliOutput.apply());
    }

    public static CliApplicationBuilder apply(ActorSystem system) {
        return apply(ActorMaterializer.create(system));
    }

    public CliApplicationBuilder withAboutCommand(AboutResource resource) {
        this.cli.addSubcommand("about", AboutCommand.apply(out, resource, mat));
        return this;
    }

    public CliApplication withArguments(Iterable<String> arguments) {
        return CliApplication.apply(ImmutableList.copyOf(arguments), cli, out);
    }

    public CliApplication withArguments(String... arguments) {
        return withArguments(Lists.newArrayList(arguments));
    }

    public CliApplicationBuilder withPrintStream(CliOutput out) {
        this.out = out;
        return this;
    }

}