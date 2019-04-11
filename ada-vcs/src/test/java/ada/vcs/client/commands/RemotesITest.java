package ada.vcs.client.commands;

import ada.vcs.client.features.ApplicationContext;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class RemotesITest {

    private Path dir;

    private Path remotesDir;

    private Path remoteDir$01;

    private Path remoteDir$02;

    private ApplicationContext context;

    @Before
    public void setup() throws IOException {
        context = new ApplicationContext();
        dir = Files.newTemporaryFolder().toPath();
        remotesDir = Files.newTemporaryFolder().toPath();
        remoteDir$01 = remotesDir.resolve("first-remote");
        remoteDir$02 = remotesDir.resolve("second-remote");

        java.nio.file.Files.createDirectory(remoteDir$01);
        java.nio.file.Files.createDirectory(remoteDir$02);

        System.setProperty("user.dir", dir.toAbsolutePath().toString());
    }

    @After
    public void cleanup() {
        if (dir != null) {
            Files.delete(dir.toFile());
            dir = null;
        }

        if (remotesDir != null) {
            Files.delete(remotesDir.toFile());
            dir = null;
        }
    }

    @Test
    public void test() {
        final String fsRemoteName$01 = remoteDir$01.getFileName().toString();
        final String fsRemoteName$02 = remoteDir$02.getFileName().toString();

        // Given an initialized project
        context.run("init");

        context.run("remotes", "add", "http://foo.bar/http-remote");
        assertThat(context.getOutput()).contains("Added new remote 'http-remote'.");

        context.run("remotes", "add", "file:./" + dir.relativize(remoteDir$01), "--verbose");
        assertThat(context.getOutput()).contains("Added new remote '" + fsRemoteName$01 + "'.");

        context.run("remotes", "add", "file://" + remoteDir$02.toAbsolutePath(), "--verbose");
        assertThat(context.getOutput()).contains("Added new remote '" + fsRemoteName$02 + "'.");

        context.run("remotes", "add", "http://foo.bar/http-remote", "hippo-remote");
        assertThat(context.getOutput()).contains("Added new remote 'hippo-remote'.");

        context.clearOutput();


        context.run("remotes");
        assertThat(context.getOutput())
            .contains("*    http-remote")
            .contains(fsRemoteName$01)
            .contains(fsRemoteName$02)
            .contains("hippo-remote");

        System.out.println(context.getOutput());
    }

}
