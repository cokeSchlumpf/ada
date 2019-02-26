package com.ibm.ada.model.versions;

import com.ibm.ada.exceptions.InvalidVersionStringException;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "apply")
public class Version {

    private final int major;

    private final int minor;

    /**
     * Used for de-serialization.
     */
    @SuppressWarnings("unused")
    private Version() {
        this(0, 0);
    }

    /**
     * Creates a Version based on a string.
     *
     * @param versionString The string representation of the version.
     * @return The parsed version
     * @throws InvalidVersionStringException If string cannot be parsed as version.
     */
    public static Version apply(String versionString) {
        try {
            String[] parts = versionString.split("[.]");

            int major = Integer.valueOf(parts[0]);
            int minor = Integer.valueOf(parts[1]);

            return Version.apply(major, minor);
        } catch (Exception e) {
            throw InvalidVersionStringException.apply(versionString);
        }
    }

    /**
     * Returns a string representation of the string.
     *
     * @return a string representation.
     */
    public String versionString() {
        return String.format("%d.%d", major, minor);
    }

}
