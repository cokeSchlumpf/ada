package com.ibm.ada.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.ada.common.ObjectMapperFactory;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class HttpEndpointUTest {

    @Test
    public void test() throws IOException {
        String url = "http://www.google.de/foo/bar";
        ObjectMapper om = ObjectMapperFactory.apply().create();
        HttpEndpoint e = HttpEndpoint.apply(new URL("http://www.google.de/foo/bar"));

        String json = om.writeValueAsString(e);

        assertThat(json).contains(url);
        assertThat(om.readValue(json, HttpEndpoint.class)).isEqualTo(e);
    }

}
