package ada.vcs.client.commands;

import lombok.AllArgsConstructor;
import picocli.CommandLine;

import java.util.Optional;

@CommandLine.Command(
    name = "add",
    description = "adds a new target to the dataset",
    subcommands = {
        Dataset$Targets$Add$Avro.class,
        Dataset$Targets$Add$CSV.class
    })
@AllArgsConstructor(staticName = "apply")
public final class Dataset$Targets$Add extends StandardOptions {

    @CommandLine.ParentCommand
    private Dataset$Targets targets;

    public static Dataset$Targets$Add apply() {
        return apply(null);
    }

    public Optional<Dataset$Targets> getTargets() {
        return Optional.ofNullable(targets);
    }
}
