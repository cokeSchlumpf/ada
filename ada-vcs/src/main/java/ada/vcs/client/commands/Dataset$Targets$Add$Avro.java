package ada.vcs.client.commands;

import ada.commons.util.ResourceName;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.converters.avro.AvroSink;
import ada.vcs.client.core.project.AdaProject;
import ada.vcs.client.core.dataset.Target;
import org.apache.commons.io.FilenameUtils;
import picocli.CommandLine;

import java.io.File;
import java.util.Optional;

@CommandLine.Command(
    name = "avro",
    description = "adds an Avro target to the dataset")
public final class Dataset$Targets$Add$Avro extends StandardOptions implements ProjectCommand {

    private final CommandLineConsole console;

    @CommandLine.ParentCommand
    private Dataset$Targets$Add add;

    @CommandLine.Parameters(index = "1",
        arity = "0..1",
        paramLabel = "ALIAS",
        description = "the alias for the target")
    private String alias;

    @CommandLine.Parameters(index = "0",
        paramLabel = "FILE",
        description = "the Avro file which should be written")
    private File file;

    private Dataset$Targets$Add$Avro(CommandLineConsole console) {
        this.console = console;
    }

    public static Dataset$Targets$Add$Avro apply(CommandLineConsole console) {
        return new Dataset$Targets$Add$Avro(console);
    }

    @Override
    public void run(AdaProject project) {
        Dataset dataset = getAdd()
            .flatMap(Dataset$Targets$Add::getTargets)
            .flatMap(Dataset$Targets::getDataset)
            .orElseThrow(() -> new IllegalStateException(""));

        if (alias == null) {
            alias = FilenameUtils.removeExtension(file.getName());
        }

        AvroSink sink = AvroSink.apply(file.toPath());
        project.addTarget(
            dataset.getAlias(),
            Target.apply(ResourceName.apply(alias), sink.relativize(project.getPath())));

        console.message("Added Avro target '%s' to dataset '%s'.", alias, dataset.getAlias());
    }

    public Optional<Dataset$Targets$Add> getAdd() {
        return Optional.ofNullable(add);
    }
}
