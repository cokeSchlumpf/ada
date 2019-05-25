package ada.domain.legacy.datatypes;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply", access = AccessLevel.PACKAGE)
public final class Proximity {

    private double correct;

    private double nullValues;

    private double invalidValues;

    static Proximity apply() {
        return apply(0, 0, 0);
    }

    void countNullValue() {
        this.nullValues++;
    }

    void countCorrectValue() {
        this.correct++;
    }

    void countInvalidValue() {
        this.invalidValues++;
    }

    public double support() {
        return (correct + nullValues) / (correct + nullValues + invalidValues);
    }

    public double confidence() {
        return correct / (correct + nullValues + invalidValues);
    }

    @Override
    public String toString() {
        return String.format(
            "Proximity(support=%.2f, confidence=%.2f, correct=%.2f, null=%.2f, invalid=%.2f)",
            support(), confidence(), correct, nullValues, invalidValues);
    }

}
