package ada.vcs.client.commands;

import ada.commons.util.ResourceName;
import ada.vcs.client.consoles.CommandLineConsole;
import ada.vcs.client.converters.csv.CSVSource;
import ada.vcs.client.core.dataset.DatasetImpl;
import ada.vcs.client.core.project.AdaProject;
import ada.vcs.client.core.dataset.Dataset;
import org.apache.commons.io.FilenameUtils;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

@CommandLine.Command(
    name = "csv",
    description = "adds a new dataset from CSV file")
public final class Datasets$Add$CSV extends StandardOptions implements ProjectCommand {

    private final CommandLineConsole console;

    private final CommandContext context;

    @CommandLine.Parameters(index = "1",
        arity = "0..1",
        paramLabel = "ALIAS",
        description = "the alias for the data-set")
    private String alias;

    @CommandLine.Parameters(index = "0",
        paramLabel = "FILE",
        description = "the CSV source file")
    private File file;

    @CommandLine.Option(
        names = {"-f", "--field-separator"},
        defaultValue = ",",
        description = "the column separator used in the csv file")
    private char fieldSeparator;

    @CommandLine.Option(
        names = {"-c", "--comment"},
        defaultValue = "#",
        description = "the character which indicates comment lines")
    private char commentChar;

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
        names = {"-n", "--headers"},
        defaultValue = "",
        description = "supply name of column headers if not contained in the file")
    private List<String> headers;

    @CommandLine.Option(
        names = {"-a", "--analyze"},
        defaultValue = "100",
        description = "the number of records which are analyzed to detect missing information (e.g. schema)")
    private int recordsAnalyzed;

    private Datasets$Add$CSV(CommandLineConsole console, CommandContext context) {
        this.console = console;
        this.context = context;
    }

    public static Datasets$Add$CSV apply(CommandLineConsole console, CommandContext context) {
        return new Datasets$Add$CSV(console, context);
    }

    @Override
    public void run(AdaProject project) {
        if (alias == null) {
            alias = FilenameUtils.removeExtension(file.getName());
        }

        context.withMaterializer(materializer -> {
            CSVSource source = CSVSource.apply(file.toPath(), fieldSeparator, commentChar, quoteChar, escapeChar, headers, recordsAnalyzed);

            return source
                .analyze(context.getMaterializer())
                .thenApply(readable -> {
                    Dataset ds = DatasetImpl.apply(
                        ResourceName.apply(alias),
                        source.relativize(project.getPath()),
                        readable.getSchema());

                    project.addDataset(ds);

                    console.message(String.format("Added dataset '%s'.", ds.getAlias().getValue()));
                    return readable;
                });
        });
    }

}
