package ada.commons.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

@Value
@Wither
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "apply")
public class FileSize {

    private static final long ONE_BYTE = 1;
    private static final long ONE_KB = 1024;
    private static final long ONE_MB = ONE_KB * ONE_KB;
    private static final long ONE_GB = ONE_MB * ONE_KB;
    private static final long ONE_TB = ONE_GB * ONE_KB;

    private final long size;
    private final Unit unit;

    public long getBytes() {
        return getSize() * getUnit().getFactor();
    }

    public enum Unit {
        BYTES(ONE_BYTE, "Bytes"),
        KILOBYTES(ONE_KB, "KB"),
        MEGABYTES(ONE_MB, "MB"),
        GIGABYTES(ONE_GB, "GB"),
        TERABYTES(ONE_TB, "TB");

        private final long factor;
        private final String name;

        Unit(long factor, String name) {
            this.factor = factor;
            this.name = name;
        }

        public long getFactor() {
            return factor;
        }

        public String getName() {
            return name;
        }
    }

    public String toSizeAdaptedString() {
        Unit unit = Unit.BYTES;
        double size = getBytes();

        double bytes = getBytes();

        if (bytes > ONE_TB) {
            unit = Unit.TERABYTES;
            size = bytes / ONE_TB;
        } else if (bytes > ONE_GB) {
            unit = Unit.GIGABYTES;
            size = bytes / ONE_GB;
        } else if (bytes > ONE_MB) {
            unit = Unit.MEGABYTES;
            size = bytes / ONE_MB;
        } else if (bytes > ONE_KB) {
            unit = Unit.KILOBYTES;
            size = bytes / ONE_KB;
        }


        return String.format("%.2f %s", size, unit.name);

    }

}
