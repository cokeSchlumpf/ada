package ada.vcs.client.core.remotes;

import ada.commons.util.ResourceName;
import ada.vcs.client.util.AbstractAdaTest;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class RemotesUTest extends AbstractAdaTest {

    @Test
    public void testJson() throws IOException {
        RemotesFactory factory = getContext().factories().remotesFactory();
        Remote fsRemoteOrig = factory.createFileSystemRemote(ResourceName.apply("fs-remote"), Paths.get("test"));
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        fsRemoteOrig.writeTo(os);
        String fsRemoteJson = new String(os.toByteArray(), StandardCharsets.UTF_8);
        Remote fsRemoteRead = factory.createRemote(new ByteArrayInputStream(fsRemoteJson.getBytes()));

        assertThat(fsRemoteRead).isEqualTo(fsRemoteOrig);

        Remote httpRemoteOrig = factory.createHttpRemote(ResourceName.apply("http-remote"), new URL("http://foo.bar/project"));

        os.reset();
        httpRemoteOrig.writeTo(os);
        String httpRemoteJson = new String(os.toByteArray(), StandardCharsets.UTF_8);
        Remote httpRemoteRead = factory.createRemote(new ByteArrayInputStream(httpRemoteJson.getBytes()));

        assertThat(httpRemoteRead).isEqualTo(httpRemoteOrig);

        Remotes remotesOrig = factory.createRemotes(Lists.newArrayList(fsRemoteOrig, httpRemoteOrig), fsRemoteOrig.alias());
        os.reset();

        remotesOrig.writeTo(os);
        String remotesJson = new String(os.toByteArray(), StandardCharsets.UTF_8);
        Remotes remotesRead = factory.createRemotes(new ByteArrayInputStream(remotesJson.getBytes()));

        assertThat(remotesRead).isEqualTo(remotesOrig);

        System.out.println(fsRemoteJson);
        System.out.println(httpRemoteJson);
        System.out.println(remotesJson);
    }

    @Test
    public void test() throws MalformedURLException {
        RemotesFactory factory = getContext().factories().remotesFactory();
        Remote fsRemote = factory.createFileSystemRemote(ResourceName.apply("fs-remote"), Paths.get("test"));
        Remote httpRemote = factory.createHttpRemote(ResourceName.apply("http-remote"), new URL("http://foo.bar"));

        Remotes remotes = factory
            .createRemotes()
            .add(fsRemote);

        assertThat(remotes.getRemotes().collect(Collectors.toList()))
            .contains(fsRemote)
            .hasSize(1);
        assertThat(remotes.getUpstream().isPresent()).isTrue();
        assertThat(remotes.getUpstream().orElse(null)).isEqualTo(fsRemote);

        remotes = remotes.add(httpRemote);

        assertThat(remotes.getRemotes().collect(Collectors.toList()))
            .contains(fsRemote)
            .contains(httpRemote)
            .hasSize(2);
        assertThat(remotes.getUpstream().isPresent()).isTrue();
        assertThat(remotes.getUpstream().orElse(null)).isEqualTo(fsRemote);

        assertThat(remotes.getRemote(fsRemote.alias()).orElse(null)).isEqualTo(fsRemote);
        assertThat(remotes.getRemote("foo").isPresent()).isFalse();

        remotes = remotes.add(fsRemote);
        remotes = remotes.add(httpRemote);

        assertThat(remotes.getRemotes().collect(Collectors.toList()))
            .contains(fsRemote)
            .contains(httpRemote)
            .hasSize(2);
        assertThat(remotes.getUpstream().isPresent()).isTrue();
        assertThat(remotes.getUpstream().orElse(null)).isEqualTo(fsRemote);

        remotes = remotes.remove(fsRemote);

        assertThat(remotes.getRemotes().collect(Collectors.toList()))
            .contains(httpRemote)
            .hasSize(1);
        assertThat(remotes.getUpstream().isPresent()).isTrue();
        assertThat(remotes.getUpstream().orElse(null)).isEqualTo(httpRemote);

        remotes = remotes.remove(httpRemote);

        assertThat(remotes.getRemotes().collect(Collectors.toList())).isEmpty();
        assertThat(remotes.getUpstream().isPresent()).isFalse();

    }

}
