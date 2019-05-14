package ada.vcs.adapters.cli.commands;

import ada.commons.util.FileSize;
import ada.vcs.client.features.ApplicationContext;
import ada.vcs.client.util.AbstractAdaTest;
import ada.vcs.client.util.TestDataFactory;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class RemotesITest extends AbstractAdaTest {

    private Path remotesDir;

    private Path remoteDir$01;

    private Path remoteDir$02;

    @Before
    public void before() throws Exception {
        super.before();

        remotesDir = Files.newTemporaryFolder().toPath();
        remoteDir$01 = remotesDir.resolve("first-remote");
        remoteDir$02 = remotesDir.resolve("second-remote");

        java.nio.file.Files.createDirectory(remoteDir$01);
        java.nio.file.Files.createDirectory(remoteDir$02);
    }

    @After
    public void after() throws Exception {
        super.after();

        if (remotesDir != null) {
            Files.delete(remotesDir.toFile());
            remotesDir = null;
        }
    }

    @Test
    public void test() {
        final ApplicationContext context = getApplication();
        final Path dir = getDirectory();
        final Path outputCSV = dir.resolve("out-csv.csv");

        final String host = getServer();

        final String fsRemoteName$01 = remoteDir$01.getFileName().toString();
        final String fsRemoteName$02 = remoteDir$02.getFileName().toString();
        final String fooFile = TestDataFactory.createSampleCSVFile(
            dir, "foo.csv", FileSize.apply(1, FileSize.Unit.MEGABYTES)).toAbsolutePath().toString();

        context.run("init", "--time", "--verbose");

        context.run("remotes", "add", "http://foo.bar/http-remote", "--verbose");
        assertThat(context.getOutput()).contains("Added new remote 'http-remote'.");

        context.run("remotes", "add", "file:./" + dir.relativize(remoteDir$01), "--verbose");
        assertThat(context.getOutput()).contains("Added new remote '" + fsRemoteName$01 + "'.");

        context.run("remotes", "add", "file://" + remoteDir$02.toAbsolutePath(), "--verbose");
        assertThat(context.getOutput()).contains("Added new remote '" + fsRemoteName$02 + "'.");

        context.run("remotes", "add", "http://" + host + "/public/hello", "hippo-remote");
        assertThat(context.getOutput()).contains("Added new remote 'hippo-remote'.");

        context.clearOutput();


        context.run("remotes");
        assertThat(context.getOutput())
            .contains("* http-remote")
            .contains(fsRemoteName$01)
            .contains(fsRemoteName$02)
            .contains("hippo-remote");

        context.clearOutput();

        context.run("datasets", "add", "csv", fooFile, "foo", "-f", ";", "-a", "100");
        context.run("datasets", "push", fsRemoteName$02, "--time", "--verbose");
        context.run("datasets", "push", "hippo-remote", "--time", "--verbose");

        context.run("dataset", "foo", "targets", "add", "csv", outputCSV.toString(), "--verbose");
        assertThat(context.getOutput())
            .contains("Uploading data to from dataset 'foo' (1 of 1)")
            .contains("Pushed ref");
        context.clearOutput();

        context.run("dataset", "foo", "extract", "--verbose", "--time");
        assertThat(context.getOutput())
            .contains("Starting to extract target 'out-csv' (1 of 1)")
            .contains("records to target 'out-csv'")
            .contains("Extracted");
        assertThat(java.nio.file.Files.exists(outputCSV)).isTrue();
        context.clearOutput();

        Files.delete(outputCSV.toFile());

        context.run("dataset", "foo", "extract", "--original", "--verbose", "--time");
        assertThat(java.nio.file.Files.exists(outputCSV)).isTrue();
        context.clearOutput();

        context.run("datasets", "extract");
        context.clearOutput();
    }

}
