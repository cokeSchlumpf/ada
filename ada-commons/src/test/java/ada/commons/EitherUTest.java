package ada.commons;

import ada.commons.databind.ObjectMapperFactory;
import ada.commons.util.Either;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class EitherUTest {

    @Test
    public void test() throws IOException {
        ObjectMapper om = ObjectMapperFactory.apply().create(true);
        JavaType valueType = om.getTypeFactory().constructParametricType(Either.class, String.class, Integer.class);

        Either<String, Integer> er = Either.right(42);
        String erj = om.writeValueAsString(er);

        Either<String, Integer> erp = om.readValue(erj, valueType);
        assertThat(erp).isEqualTo(er);

        Either<String, Integer> el = Either.left("Hello");
        String elj = om.writeValueAsString(el);

        Either<String, Integer> elp = om.readValue(elj, valueType);
        assertThat(elp).isEqualTo(el);

        System.out.println(erj);
        System.out.println(elj);
    }

}
