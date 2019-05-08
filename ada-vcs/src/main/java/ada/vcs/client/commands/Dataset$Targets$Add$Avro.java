package ada.vcs.client.commands;

import ada.commons.util.ResourceName;
import ada.vcs.client.commands.context.CommandContext;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.shared.converters.avro.AvroSink;
import ada.vcs.client.exceptions.CommandNotInitializedException;
import org.apache.commons.io.FilenameUtils;
import picocli.CommandLine;

import java.io.File;
import java.util.Optional;

@CommandLine.Command(
    name = "avro",
    description = "adds an Avro target to the dataset")
public final class Dataset$Targets$Add$Avro extends StandardOptions implements Runnable {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.ParentCommand
    private Dataset$Targets$Add add = null;

    @CommandLine.Parameters(index = "1",
        arity = "0..1",
        paramLabel = "ALIAS",
        description = "the alias for the target")
    private String alias;

    @CommandLine.Parameters(index = "0",
        paramLabel = "FILE",
        description = "the Avro file which should be written")
    private File file = null;

    private Dataset$Targets$Add$Avro(CommandLineConsole console, CommandContext context) {
        this.console = console;
        this.context = context;
    }

    public static Dataset$Targets$Add$Avro apply(CommandLineConsole console, CommandContext context) {
        return new Dataset$Targets$Add$Avro(console, context);
    }

    @Override
    public void run() {
        context.withProject(project -> {
            Dataset dataset = getAdd()
                .flatMap(Dataset$Targets$Add::getTargets)
                .flatMap(Dataset$Targets::getDataset)
                .orElseThrow(CommandNotInitializedException::apply);

            if (alias == null) {
                alias = FilenameUtils.removeExtension(file.getName());
            }

            if (!file.isAbsolute()) {
                file = project
                    .path()
                    .resolve(file.toPath())
                    .normalize()
                    .toFile();
            }

            project.addGitIgnore(file.toPath(), false, "avcs extracted file");

            AvroSink sink = AvroSink
                .apply(file.toPath())
                .relativize(project.path());

            project.addTarget(
                dataset.alias(),
                context
                    .factories()
                    .datasetFactory()
                    .createTarget(ResourceName.apply(alias), sink));

            console.message("Added Avro target '%s' to dataset '%s'.", alias, dataset.alias());
        });
    }

    public Optional<Dataset$Targets$Add> getAdd() {
        return Optional.ofNullable(add);
    }
}
