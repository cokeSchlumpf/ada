package ada.commons.databind;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.avro.Schema;

import java.nio.file.Path;

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

    public ObjectMapper create(boolean pretty) {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new GuavaModule());
        om.registerModule(new Jdk8Module());
        om.registerModule(new JavaTimeModule());

        om.getSerializationConfig().getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withCreatorVisibility(JsonAutoDetect.Visibility.ANY);

        SimpleModule module = new SimpleModule();
        module.addSerializer(Schema.class, new SchemaSerializer());
        module.addDeserializer(Schema.class, new SchemaDeserializer());
        module.addSerializer(Path.class, new PathSerializer());
        module.addDeserializer(Path.class, new PathDeserializer());
        om.registerModule(module);

        if (pretty) {
            om.enable(SerializationFeature.INDENT_OUTPUT);
        }

        om.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return om;
    }

    public ObjectMapper create() {
        return create(false);
    }

}
