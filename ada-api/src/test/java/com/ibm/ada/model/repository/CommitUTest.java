package com.ibm.ada.model.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.ada.common.ObjectMapperFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CommitUTest {

    @Test
    public void test() throws IOException {
        Commit commit = Commit.apply("abcd", "my message", new Date(), "edgar");
        ObjectMapper om = ObjectMapperFactory.apply().create();

        String json = om.writeValueAsString(commit);
        assertThat(om.readValue(json, Commit.class)).isEqualTo(commit);
    }

}
