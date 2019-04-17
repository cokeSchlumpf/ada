package ada.vcs.client.core.remotes;

import ada.commons.databind.ObjectMapperFactory;
import ada.commons.util.ResourceName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class RemotesUTest {

    @Test
    public void testJson() throws IOException {
        ObjectMapper om = ObjectMapperFactory.apply().create(true);

        FileSystemRemote fsRemoteOrig = FileSystemRemote.apply(ResourceName.apply("fs-remote"), Paths.get("test"));
        String fsRemoteJson = om.writeValueAsString(fsRemoteOrig);
        FileSystemRemote fsRemoteRead = om.readValue(fsRemoteJson, FileSystemRemote.class);

        assertThat(fsRemoteRead).isEqualTo(fsRemoteOrig);

        HttpRemote httpRemoteOrig = HttpRemote.apply(ResourceName.apply("http-remote"), new URL("http://foo.bar/project"));
        String httpRemoteJson = om.writeValueAsString(httpRemoteOrig);
        HttpRemote httpRemoteRead = om.readValue(httpRemoteJson, HttpRemote.class);

        assertThat(httpRemoteRead).isEqualTo(httpRemoteOrig);

        Remotes remotesOrig = RemotesImpl.apply(Lists.newArrayList(fsRemoteOrig, httpRemoteOrig), fsRemoteOrig.alias());
        String remotesJson = om.writeValueAsString(remotesOrig);
        Remotes remotesRead = om.readValue(remotesJson, RemotesImpl.class);

        assertThat(remotesRead).isEqualTo(remotesOrig);

        System.out.println(fsRemoteJson);
        System.out.println(httpRemoteJson);
        System.out.println(remotesJson);
    }

    @Test
    public void test() throws MalformedURLException {
        FileSystemRemote fsRemote = FileSystemRemote.apply(ResourceName.apply("fs-remote"), Paths.get("test"));
        HttpRemote httpRemote = HttpRemote.apply(ResourceName.apply("http-remote"), new URL("http://foo.bar"));

        Remotes remotes = RemotesImpl
            .apply()
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
