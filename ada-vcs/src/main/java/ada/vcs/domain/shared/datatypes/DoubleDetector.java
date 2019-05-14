package ada.vcs.domain.shared.datatypes;

import ada.commons.util.Either;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public final class DoubleDetector implements DataTypeDetector<DoubleDetector> {

    private final List<Locale> locales;

    private final NumberFormat defaultNumberFormat;

    private boolean isOptional;

    private NumberFormat numberFormat;

    private Proximity proximity;

    public static DoubleDetector apply(List<Locale> locales, NumberFormat defaultNumberFormat) {
        return apply(ImmutableList.copyOf(locales), defaultNumberFormat, false, null, Proximity.apply());
    }

    public static DoubleDetector apply() {
        List<Locale> locales = Lists.newArrayList(
            Locale.ENGLISH,
            Locale.GERMAN);

        return apply(locales, NumberFormat.getInstance(locales.get(0)));
    }

    @Override
    public Proximity getProximity() {
        return proximity;
    }

    @Override
    public void hint(String value) {
        if (isNullValue(value)) {
            isOptional = true;
            proximity.countNullValue();
            return;
        }

        if (numberFormat == null) {
            numberFormat = locales
                .stream()
                .map(NumberFormat::getInstance)
                .filter(nf -> parse(value, nf).map(i -> true, i -> false))
                .findFirst()
                .orElse(null);
        }

        if (numberFormat == null) {
            proximity.countInvalidValue();
        } else {
            parse(value, numberFormat).apply(
                i -> proximity.countCorrectValue(),
                i -> proximity.countInvalidValue());
        }
    }

    @Override
    public DoubleDetector withOptional(boolean isOptional) {
        this.isOptional = isOptional;
        return this;
    }

    @Override
    public Field type(String fieldName) {
        DoubleDetectorTypeHelper h = DoubleDetectorTypeHelper.apply(
            fieldName,
            isOptional,
            Optional.ofNullable(numberFormat).orElse(defaultNumberFormat));

        return DetectedField.apply(fieldName, "Double", h, h);
    }

    private static Either<Object, Exception> parse(String value, NumberFormat nf) {
        return Either.result(() -> nf.parse(value).doubleValue());
    }

    private static boolean isNullValue(String value) {
        return value == null || value.trim().length() == 0;
    }

    @AllArgsConstructor(staticName = "apply")
    private static class DoubleDetectorTypeHelper implements DetectedField.Builder, DetectedField.Parser {

        private final String fieldName;

        private final boolean isOptional;

        private final NumberFormat format;

        @Override
        public Function<SchemaBuilder.FieldAssembler<Schema>, SchemaBuilder.FieldAssembler<Schema>> build() {
            if (isOptional) {
                return builder -> builder.optionalDouble(fieldName);
            } else {
                return builder -> builder.requiredDouble(fieldName);
            }
        }

        @Override
        public Either<Object, Exception> parse(String value) {
            boolean isNull = isNullValue(value);

            if (isNull && isOptional) {
                return Either.left(null);
            } else if (isNull) {
                return Either.right(DetectedField.NotOptionalException.apply(fieldName));
            } else {
                return DoubleDetector.parse(value, format);
            }
        }

    }

}
