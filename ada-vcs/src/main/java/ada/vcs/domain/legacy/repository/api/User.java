package ada.vcs.domain.legacy.repository.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    private final String name;

    private final String email;

    @JsonCreator
    public static User apply(
        @JsonProperty("name") String name,
        @JsonProperty("email") String email) {

        if (email != null && email.trim().length() == 0) {
            email = null;
        }

        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("`name` must not be null or empty");
        }

        return new User(name, email);
    }

    public static User apply(String name) {
        return apply(name, null);
    }

    public static User fromString(String s) {
        Pattern pattern = Pattern.compile("([^<]+)\\s<([^>]+)>");
        Matcher matcher = pattern.matcher(s);

        if (!matcher.matches()) {
            String message = String.format("`%s` is not a valid User string", s);
            throw new IllegalArgumentException(message);
        }

        return apply(matcher.group(1), matcher.group(2));
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    @Override
    public String toString() {
        return String.format("%s <%s>", name, getEmail().orElse(""));
    }

    public static class FakeImpl {

        public static User apply() {
            return User.apply("Egon Olsen", "egon@olsenbande.dk");
        }

    }

}
