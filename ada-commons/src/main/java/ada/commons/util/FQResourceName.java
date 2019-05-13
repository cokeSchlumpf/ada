package ada.commons.util;

import ada.commons.exceptions.InvalidResourceNameException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Optional;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FQResourceName {

    private final ResourceName namespace;

    private final ResourceName name;

    @JsonCreator
    public static FQResourceName apply(
        @JsonProperty("namespace") ResourceName namespace,
        @JsonProperty("name") ResourceName name) {

        return new FQResourceName(namespace, name);
    }

    public static FQResourceName apply(String s) {
        try {
            String[] parts = s.split("/");
            ResourceName namespace = ResourceName.apply(parts[0]);
            ResourceName name = ResourceName.apply(parts[1]);

            return new FQResourceName(namespace, name);
        } catch (Exception e) {
            throw InvalidResourceNameException.apply(s);
        }
    }

    public static Optional<FQResourceName> tryApply(String s) {
        return Operators.exceptionToNone(() -> apply(s));
    }

    @Override
    public String toString() {
        return String.format("%s/%s", namespace, name);
    }

}
