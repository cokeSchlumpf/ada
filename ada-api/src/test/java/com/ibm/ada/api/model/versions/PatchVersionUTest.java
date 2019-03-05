package com.ibm.ada.api.model.versions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.ada.api.exceptions.InvalidVersionStringException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PatchVersionUTest {

    @Test
    public void test() {
        assertThat(PatchVersion.apply(2, 42, 13, true).versionString()).isEqualTo("2.42.13");
        assertThat(PatchVersion.apply(2, 42, 13, false).versionString()).isEqualTo("2.42.13-snapshot");

        assertThat(PatchVersion.apply("3.42").versionString()).isEqualTo("3.42.0");
        assertThat(PatchVersion.apply("3.13.42").versionString()).isEqualTo("3.13.42");
        assertThat(PatchVersion.apply("3.13.42-snapshot").versionString()).isEqualTo("3.13.42-snapshot");

        assertThatThrownBy(() -> PatchVersion.apply("")).isInstanceOf(InvalidVersionStringException.class);
    }

    @Test
    public void testJson() throws IOException {
        SomeObject obj = SomeObject.apply(PatchVersion.apply(1,13,42,false));
        ObjectMapper om = new ObjectMapper();

        assertThat(om.writeValueAsString(obj)).isEqualTo("{\"version\":\"1.13.42-snapshot\"}");
        assertThat(om.readValue("{\"version\":\"2.13\"}", SomeObject.class).getVersion().versionString())
            .isEqualTo("2.13.0");
    }

    @Value
    @NoArgsConstructor(force = true)
    @AllArgsConstructor(staticName = "apply")
    public static class SomeObject {

        PatchVersion version;

    }

}
