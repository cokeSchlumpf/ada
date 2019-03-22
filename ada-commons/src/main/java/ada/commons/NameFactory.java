package ada.commons;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor(staticName = "apply")
public final class NameFactory {

    private final int minLength;

    private final boolean toLowerCase;

    private final Map<String, String> replacements;

    private final Pattern startsWithCharacters;

    private final Pattern validCharacters;

    public static NameFactory apply() {
        return apply(Defaults.LOWERCASE_HYPHENATE);
    }

    public static NameFactory apply(Defaults setting) {
        Map<String, String> replacements = Maps.newHashMap();
        replacements.put(" ", "-");
        replacements.put("Ä", "Ae");
        replacements.put("ä", "ae");
        replacements.put("Ö", "Oe");
        replacements.put("ö", "oe");
        replacements.put("Ü", "Ue");
        replacements.put("ü", "ue");
        replacements.put("ß", "ss");

        switch (setting) {
            case LOWERCASE:
                return apply(1, true, replacements, Pattern.compile("^[a-zA-Z].*$"), Pattern.compile("[a-zA-Z0-9]+"));
            case LOWERCASE_UNDERSCORED:
                replacements.put("-", "_");
                return apply(1, true, replacements, Pattern.compile("^[a-zA-Z].*$"), Pattern.compile("[a-zA-Z0-9_]+"));
            default:
                return apply(1, true, replacements, Pattern.compile("^[a-zA-Z].*$"), Pattern.compile("[a-zA-Z0-9\\-]+"));
        }
    }

    public String create(String s) {
        Pair<String, InvalidNameException> r = create$internal(s);

        if (r.getRight() != null) {
            throw r.getRight();
        } else {
            return r.getLeft();
        }
    }

    public String create(String s, String defaultValue) {
        Pair<String, InvalidNameException> r = create$internal(s);

        if (r.getRight() != null) {
            return defaultValue;
        } else {
            return r.getLeft();
        }
    }

    /**
     * Internal mechanism to avoid creation of stack trace etc. when throwing InvalidNameException in case
     * of using {@link NameFactory#create(String, String)}.
     * <p>
     * Unfortunately we don't have an Either type, thus we miss-use the Pair type.
     *
     * @param s The string to check.
     * @return A pair, if the value is not valid, right size will contain a {@link InvalidNameException}, otherwise the left side
     * will contain the transformed value.
     */
    private Pair<String, InvalidNameException> create$internal(String s) {
        if (s == null) {
            return Pair.of(null, InvalidNameException.apply());
        }
        String orig = s;
        if (toLowerCase)
            s = s.toLowerCase();
        s = replaceAll(s);
        s = getValidCharacters(s);

        if (s.length() < minLength) {
            return Pair.of(null, InvalidNameException.apply(orig, s, minLength));
        }

        if (isInvalidPrefix(s)) {
            return Pair.of(null, InvalidNameException.apply(orig, s, startsWithCharacters));
        }

        return Pair.of(s, null);
    }

    private String replaceAll(String s) {
        for (Map.Entry<String, String> replacement : replacements.entrySet()) {
            s = s.replace(replacement.getKey(), replacement.getValue());
        }

        return s;
    }

    private boolean isInvalidPrefix(String s) {
        Matcher m = startsWithCharacters.matcher(s);
        return !m.matches();
    }

    private String getValidCharacters(String s) {
        Matcher m = validCharacters.matcher(s);
        StringBuilder validCharactersOnly = new StringBuilder();

        while (m.find()) {
            validCharactersOnly.append(m.group());
        }

        return validCharactersOnly.toString();
    }

    public static class InvalidNameException extends RuntimeException {

        private InvalidNameException(String message) {
            super(message);
        }

        public static InvalidNameException apply(String name, String transformed, int minLength) {
            String message = String.format(
                "The provided name '%s' ('%s') is not valid; A minimum length of %d is required.",
                name,
                transformed,
                minLength);

            return new InvalidNameException(message);
        }

        public static InvalidNameException apply(String name, String transformed, Pattern pattern) {
            String message = String.format(
                "The provided name '%s' ('%s') is not valid; It must start with pattern '%s'.",
                name,
                transformed,
                pattern.pattern());

            return new InvalidNameException(message);
        }

        public static InvalidNameException apply() {
            return new InvalidNameException("The provided name is not allowed to be null.");
        }
    }

    public enum Defaults {

        LOWERCASE,
        LOWERCASE_HYPHENATE,
        LOWERCASE_UNDERSCORED

    }

}
