package ada.client.output;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This tests is testing {@link StringOutput} as well as {@link PrintStreamOutput} implicitly.
 */
public class OutputUTest {

    private StringOutput out;

    @Before
    public void before() {
        this.out = StringOutput.apply();
    }

    @Test
    public void exception() {
        String message = "";

        try {
            int a = 10;

            for (int i = -5; i < 5; i++) {
                a = a / i;
            }
        } catch (Exception e) {
            message = e.getMessage();
            out.exception(e);
        }

        assertThat(out.toString())
            .contains(message)
            .contains(this.getClass().getName());
    }

    @Test
    public void message() {
        String msg01 = "Hello %s";
        String msg02 = "Bye bye!";
        String var = "Asterix";

        out.message(msg01, var);
        out.message(msg02);

        assertThat(out.toString())
            .contains(String.format(msg01, var))
            .contains(msg02)
            .contains("\n");
    }

    @Test
    public void separator() {
        String msg01 = "Hello!";
        String msg02 = "Bye!";

        out.message(msg01);
        out.separator();
        out.message(msg02);

        assertThat(out.toString().length())
            .isGreaterThan(2);
    }

}
