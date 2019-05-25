package ada.domain.legacy.datatypes;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor(staticName = "apply")
public final class DataTypeMatcher {

    private final BooleanDetector booleanDetector;

    private final IntegerDetector integerDetector;

    private final DoubleDetector doubleDetector;

    private final EnumDetector enumDetector;

    private final StringDetector stringDetector;

    private final double minSupport;

    private final double minConfidence;

    public static DataTypeMatcher apply() {
        BooleanDetector booleanDetector = BooleanDetector.apply();
        IntegerDetector integerDetector = IntegerDetector.apply();
        DoubleDetector doubleDetector = DoubleDetector.apply();
        EnumDetector enumDetector = EnumDetector.apply();
        StringDetector stringDetector = StringDetector.apply();

        return apply(booleanDetector, integerDetector, doubleDetector, enumDetector, stringDetector, 0.8, 0.0);
    }

    public BooleanDetector getBoolean() {
        return booleanDetector;
    }

    public DoubleDetector getDouble() {
        return doubleDetector;
    }

    public EnumDetector getEnum() {
        return enumDetector;
    }

    public IntegerDetector getInteger() {
        return integerDetector;
    }

    public StringDetector getString() {
        return stringDetector;
    }

    public void hint(String value) {
        detectors().forEach(d -> d.hint(value));
    }

    public DataTypeDetector match() {

        return detectors()
            .stream()
            .filter(d ->
                d.getProximity().support() > minSupport && d.getProximity().confidence() > minConfidence)
            .findFirst()
            .orElse(stringDetector);
    }

    private List<DataTypeDetector> detectors() {
         return Lists.newArrayList(
            booleanDetector,
            integerDetector,
            doubleDetector,
            enumDetector,
            stringDetector);
    }

}
