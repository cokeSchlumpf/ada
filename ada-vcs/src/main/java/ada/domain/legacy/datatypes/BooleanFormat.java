package ada.domain.legacy.datatypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class BooleanFormat {

    @JsonProperty("true")
    private final String trueValue;

    @JsonProperty("false")
    private final String falseValue;

    @JsonCreator
    private BooleanFormat(
        @JsonProperty("true") String trueValue,
        @JsonProperty("false") String falseValue) {
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }

    public static BooleanFormat apply(String trueValue, String falseValue) {
        return new BooleanFormat(trueValue, falseValue);
    }

    public String getFalse() {
        return falseValue;
    }

    public String getTrue() {
        return trueValue;
    }

}
