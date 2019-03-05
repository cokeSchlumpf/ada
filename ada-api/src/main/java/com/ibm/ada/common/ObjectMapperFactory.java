package com.ibm.ada.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Common place to configure Jackson Object Mapper which contains all necessary configurations to
 * serialize and deserialize Ada model classes.
 */
public final class ObjectMapperFactory {

    private ObjectMapperFactory() {

    }

    public static ObjectMapperFactory apply() {
        return new ObjectMapperFactory();
    }

    public ObjectMapper create() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new GuavaModule());
        om.registerModule(new Jdk8Module());
        om.registerModule(new JavaTimeModule());

        return om;
    }

}
