package ada.vcs.client.datatypes;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor(staticName = "apply")
public final class DataTypeDetectorImpl implements DataTypeDetector {

    private final List<DataTypeDetector> detectors;

    private final double minSupport;

    private final double minConfidence;

    public static DataTypeDetectorImpl apply(List<DataTypeDetector> detectors) {
        return apply(detectors, 0.8, 0.0);
    }

    public static DataTypeDetectorImpl apply() {
        List<DataTypeDetector> detectors = Lists.newArrayList(
            BooleanDetector.apply(),
            IntegerDetector.apply(),
            DoubleDetector.apply(),
            EnumDetector.apply(),
            StringDetector.apply());

        return apply(detectors);
    }

    @Override
    public Proximity proximity() {
        return match().proximity();
    }

    @Override
    public void hint(String value) {
        detectors.forEach(d -> d.hint(value));
    }

    @Override
    public DataType type() {
        return match().type();
    }

    private DataTypeDetector match() {
        return detectors
            .stream()
            .filter(d ->
                d.proximity().support() > minSupport && d.proximity().confidence() > minConfidence)
            .findFirst()
            .orElse(detectors.get(detectors.size() - 1));
    }

}
