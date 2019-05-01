package ada.vcs.client.commands;

import ada.commons.util.FileSize;
import ada.vcs.client.features.ApplicationContext;
import ada.vcs.client.util.AbstractAdaTest;
import ada.vcs.client.util.TestDataFactory;
import org.junit.Test;

import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class DatasetsITest extends AbstractAdaTest {

    @Test
    public void test() {
        ApplicationContext context = this.getApplication();
        Path dir = this.getDirectory();

        // Given an initialized project
        context.run("init");

        /*
         * Adding new datasets.
         */

        // Given two CSV-Files in the projects root directory
        final String fooFile = TestDataFactory.createSampleCSVFile(dir, "foo.csv", FileSize.apply(200, FileSize.Unit.MEGABYTES)).toAbsolutePath().toString();
        final String barFile = TestDataFactory.createSampleCSVFile(dir, "bar.csv").toAbsolutePath().toString();

        // When we add the twi files as a dataset
        context.run("datasets", "add", "csv", fooFile, "foo", "-f", ";", "-a", "100", "--verbose", "--time");
        context.run("datasets", "add", "csv", barFile, "bar", "-f", ";", "-a", "100", "--verbose", "--time");

        // Then the CLI should print confirmations
        assertThat(context.getOutput())
            .contains("Added dataset 'foo'.")
            .contains("Added dataset 'bar'.");

        // And the ada-directory should contain metadata for both datasets.
        assertThat(dir.resolve(".ada").resolve("datasets").resolve("foo.json"))
            .exists();
        assertThat(dir.resolve(".ada").resolve("datasets").resolve("bar.json"))
            .exists();
        context.clearOutput();

        /*
         * Listing datasets.
         */

        // When we list all datasets
        context.run("datasets", "--verbose");

        // Then the output should contain both datasets
        assertThat(context.getOutput())
            .contains("csv(bar.csv)")
            .contains("csv(foo.csv)");

        // And the output list should be ordered
        assertThat(context.getOutput().indexOf("bar")).isLessThan(context.getOutput().indexOf("foo"));

        context.clearOutput();

        /*
         * Adding targets.
         */

        // When we list the current targets of 'foo'
        context.run("dataset", "foo", "targets");

        // Then we expect no existing target.
        assertThat(context.getOutput()).contains("Dataset 'foo' does not contain any targets.");

        // When adding targets to a dataset
        context.run("dataset", "foo", "targets", "add", "csv", dir.resolve("out-csv.csv").toString(), "--verbose");
        context.run("dataset", "foo", "targets", "add", "avro", dir.resolve("out-avro.avro").toString(), "--verbose");

        // Then the CLI should confirm this.
        assertThat(context.getOutput())
            .contains("Added Avro target 'out-avro' to dataset 'foo'.")
            .contains("Added CSV target 'out-csv' to dataset 'foo'.");

        context.clearOutput();

        /*
         * Listing targets.
         */

        // When listing the datasets
        context.run("dataset", "foo", "targets", "--verbose");

        // The added datasets should be listed.
        assertThat(context.getOutput())
            .contains("avro(out-avro.avro)")
            .contains("csv(out-csv.csv)");

        context.clearOutput();
        /*
         * Extracting data.
         */

        // When calling the extract command for the 'foo' dataset
        context.run("dataset", "foo", "extract", "out-avro", "--verbose", "--time");

        // Then all targets will be extracted
        assertThat(dir.resolve("out-avro.avro"))
            .exists();
        /*assertThat(dir.resolve("out-csv.csv"))
            .exists(); */

        System.out.println(context.getOutput());
    }

}
