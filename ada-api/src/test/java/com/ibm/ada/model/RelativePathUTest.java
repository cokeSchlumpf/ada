package com.ibm.ada.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.ada.common.ObjectMapperFactory;
import org.junit.ComparisonFailure;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class RelativePathUTest {

    @Test
    public void testAbsolute() {
        assertThatThrownBy(() -> RelativePath.apply(Paths.get("hallo").toAbsolutePath()))
            .isInstanceOf(ComparisonFailure.class);
    }

    @Test
    public void test() throws IOException {
        Path p = Paths.get("hallo");
        ObjectMapper om = ObjectMapperFactory.apply().create();
        RelativePath rp = RelativePath.apply(p);

        String json = om.writeValueAsString(rp);

        assertThat(json).contains("hallo");
        assertThat(om.readValue(json, RelativePath.class)).isEqualTo(rp);
    }

}
