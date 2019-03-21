package ada.vcs.client.datatypes;

import ada.commons.Either;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DataTypeDetectorUTest {

    @Test
    public void testString() {
        /*
         * Given a string detector which has received three values and one null-value.
         */
        StringDetector d = StringDetector.apply();

        d.hint("foo");
        d.hint("bar");
        d.hint("foo");
        d.hint(null);

        /*
         * When proximity is calculated ...
         *
         * Then the support should be 1.0 as all values can be handled as String
         */
        assertThat(d.proximity().support()).isEqualTo(1.0d);

        /*
         * And the confidence should be 0.75 as 3 of 4 items clearly of type String
         */
        assertThat(d.proximity().confidence()).isEqualTo(0.75d);

        /*
         * When a type is created ...
         *
         * Then a value can be parsed
         */
        assertThat((String) d.type().parse("foo").map(l -> l, r -> null)).isEqualTo("foo");
    }

    @Test
    public void testBoolean() {
        BooleanDetector d = BooleanDetector.apply();

        d.hint("true");
        d.hint("false");
        d.hint("1");
        d.hint("3.4");

        assertThat(d.proximity().support()).isEqualTo(0.5d);
        assertThat(d.proximity().confidence()).isEqualTo(0.5d);

        assertThat(d.type().parse("TRUE")).isInstanceOf(Either.Left.class);
        assertThat(d.type().parse("1")).isInstanceOf(Either.Right.class);
        assertThat((Boolean) d.type().parse("false").map(l -> l, r -> null)).isFalse();
    }

    @Test
    public void testDouble() {
        DoubleDetector d = DoubleDetector.apply();

        d.hint("1.0");
        d.hint("1,000.42");
        d.hint("42");
        d.hint(null);

        assertThat(d.proximity().support()).isEqualTo(1.0d);
        assertThat(d.proximity().confidence()).isEqualTo(0.75d);

        assertThat(d.type().parse("7.4526")).isInstanceOf(Either.Left.class);
        assertThat(d.type().parse("Hello!")).isInstanceOf(Either.Right.class);
        assertThat((Double) d.type().parse("1.42").map(l -> l, r -> null)).isEqualTo(1.42d);
    }

    @Test
    public void testEnum() {
        EnumDetector d = EnumDetector.apply();

        d.hint("low");
        d.hint("high");
        d.hint("medium");

        assertThat(d.proximity().support()).isEqualTo(1.0d);
        assertThat(d.proximity().confidence()).isEqualTo(1.0d);

        assertThat(d.type().parse("high")).isInstanceOf(Either.Left.class);
        assertThat(d.type().parse("hello")).isInstanceOf(Either.Right.class);
        assertThat((String) d.type().parse("medium").map(l -> l, r -> null)).isEqualTo("medium");

        assertThat(d.type().description().isPresent()).isTrue();
        assertThat(d.type().description().get()).contains("low", "high", "medium");
    }

    @Test
    public void testInteger() {
        IntegerDetector d = IntegerDetector.apply();

        d.hint("1.0");
        d.hint("1,000.42");
        d.hint("42");
        d.hint(null);

        assertThat(d.proximity().support()).isEqualTo(0.5d);
        assertThat(d.proximity().confidence()).isEqualTo(0.25d);

        assertThat(d.type().parse("42")).isInstanceOf(Either.Left.class);
        assertThat(d.type().parse("34.21")).isInstanceOf(Either.Right.class);
        assertThat((Integer) d.type().parse("42").map(l -> l, r -> null)).isEqualTo(42);
    }

}
