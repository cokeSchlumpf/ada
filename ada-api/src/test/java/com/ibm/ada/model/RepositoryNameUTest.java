package com.ibm.ada.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.ada.exceptions.InvalidResourceNameException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RepositoryNameUTest {

    @Test
    public void test() {
        /*
         * RepositoryName can be instantiated with any string which will be transformed to a valid
         * repository name: not empty, only lowercase letters, numbers and hyphens, no leading or trailing hyphens
         */

        assertThat(RepositoryName.apply("hello").getValue()).isEqualTo("hello");
        assertThat(RepositoryName.apply("Hello").getValue()).isEqualTo("hello");
        assertThat(RepositoryName.apply("Hello World").getValue()).isEqualTo("hello-world");
        assertThat(RepositoryName.apply("Hello World!").getValue()).isEqualTo("hello-world");
        assertThat(RepositoryName.apply("-hello ").getValue()).isEqualTo("hello");
        assertThat(RepositoryName.apply("hello 1 world!").getValue()).isEqualTo("hello-1-world");

        assertThatThrownBy(() -> RepositoryName.apply("!!!")).isInstanceOf(InvalidResourceNameException.class);
        assertThatThrownBy(() -> RepositoryName.apply("")).isInstanceOf(InvalidResourceNameException.class);
    }

    @Test
    public void testJson() throws IOException {
        SomeObject o = SomeObject.apply(RepositoryName.apply("hello world"));
        ObjectMapper om = new ObjectMapper();

        assertThat(om.writeValueAsString(o)).isEqualTo("{\"name\":\"hello-world\"}");

        assertThat(om.readValue("{\"name\":\"hello world\"}", SomeObject.class).name.getValue())
            .isEqualTo("hello-world");
    }

    @Value
    @NoArgsConstructor(force = true)
    @AllArgsConstructor(staticName = "apply")
    private static class SomeObject {

        private final RepositoryName name;

    }

}
