package com.ibm.ada.model.versions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.ada.exceptions.InvalidVersionStringException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.junit.Test;

import java.io.IOException;

public class VersionUTest {

    @Test
    public void test() {
        assertThat(Version.apply(2,42).versionString()).isEqualTo("2.42");
        assertThat(Version.apply("3.42").versionString()).isEqualTo("3.42");

        assertThatThrownBy(() -> Version.apply("")).isInstanceOf(InvalidVersionStringException.class);
    }

    @Test
    public void testJson() throws IOException {
        SomeObject obj = SomeObject.apply(Version.apply(1,13));
        ObjectMapper om = new ObjectMapper();

        assertThat(om.writeValueAsString(obj)).isEqualTo("{\"version\":\"1.13\"}");
        assertThat(om.readValue("{\"version\":\"2.13\"}", SomeObject.class).getVersion().versionString())
            .isEqualTo("2.13");
    }

    @Value
    @NoArgsConstructor(force = true)
    @AllArgsConstructor(staticName = "apply")
    public static class SomeObject {

        Version version;

    }

}
