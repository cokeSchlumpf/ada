package ada.vcs.client.commands;

import ada.commons.util.Either;
import ada.commons.util.ResourceName;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.converters.csv.CSVSink;
import ada.vcs.client.core.AdaProject;
import ada.vcs.client.core.Target;
import ada.vcs.client.datatypes.BooleanFormat;
import ada.vcs.client.exceptions.NoProjectException;
import org.apache.commons.io.FilenameUtils;
import picocli.CommandLine;

import java.io.File;
import java.util.Optional;

@CommandLine.Command(
    name = "csv",
    description = "adds CSV as target to the dataset")
public final class Dataset$Targets$Add$CSV extends StandardOptions implements Runnable {

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
        description = "the CSV target file")
    private File file;

    @CommandLine.Option(
        names = {"-f", "--field-separator"},
        defaultValue = ",",
        description = "the column separator used in the csv file")
    private char fieldSeparator;

    @CommandLine.Option(
        names = {"-l", "--eol"},
        defaultValue = "\r\n",
        description = "the character which is used to separate records")
    private String endOfLine;

    @CommandLine.Option(
        names = {"-q", "--quote"},
        defaultValue = "\"",
        description = "the character which surrounds quoted values")
    private char quoteChar;

    @CommandLine.Option(
        names = {"-e", "--escape"},
        defaultValue = "\\",
        description = "the character to escape other control characters")
    private char escapeChar;

    @CommandLine.Option(
        names = {"-n", "--null"},
        defaultValue = "",
        description = "the value which is used to indicate a null field")
    private String nullValue;

    @CommandLine.Option(
        names = {"--nf"},
        defaultValue = "#,##0.0000",
        description = "the number-format which is used to format floating-point numbers")
    private String numberFormat;

    private Dataset$Targets$Add$CSV(CommandLineConsole console) {
        this.console = console;
    }

    public static Dataset$Targets$Add$CSV apply(CommandLineConsole console) {
        return new Dataset$Targets$Add$CSV(console);
    }

    @Override
    public void run() {
        final AdaProject project = AdaProject.fromHere().orElseThrow(NoProjectException::apply);

        if (alias == null) {
            alias = FilenameUtils.removeExtension(file.getName());
        }

        Dataset dataset = getAdd()
            .flatMap(Dataset$Targets$Add::getTargets)
            .flatMap(Dataset$Targets::getDataset)
            .orElseThrow(() -> new IllegalStateException(""));

        CSVSink sink = CSVSink.apply(
            Either.left(file.toPath()), fieldSeparator, quoteChar, escapeChar, endOfLine,
            nullValue, numberFormat, BooleanFormat.apply("true", "false"));

        project.addTarget(
            dataset.getAlias(),
            Target.apply(ResourceName.apply(alias), sink.relativize(project.getPath())));

        console.message("Added CSV target '%s' to dataset '%s'.", alias, dataset.getAlias());
    }

    public Optional<Dataset$Targets$Add> getAdd() {
        return Optional.ofNullable(add);
    }
}
