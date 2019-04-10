package ada.vcs.client.consoles;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

public interface Output {

    void message(String message, Object... args);

    void print(String message);

    void println(String message);

    default void table(String[] headers, String[][] data) {
        table(headers, data, true);
    }

    default void table(String[] headers, String[][] data, boolean printHeader) {
        Map<Integer, Integer> widths = Maps.newHashMap();
        int spacing = 4;

        // calculate column widths
        for (int col = 0; col < headers.length; col++) {
            widths.put(col, headers[col].length());
        }

        for (String[] aData : data) {
            for (int col = 0; col < aData.length; col++) {
                String content = aData[col];
                int currentMaxWidth = widths.get(col) != null ? widths.get(col) : 0;

                if (content != null && (content.length() > currentMaxWidth)) {
                    widths.put(col, content.length());
                }
            }
        }

        // Print the header
        if (printHeader) {
            for (int col = 0; col < headers.length; col++) {
                String title = headers[col];
                int width = widths.get(col) != null ? widths.get(col) : title.length();
                String spaces = StringUtils.repeat(" ", width - title.length() + spacing);

                print(String.format("%s%s", title.toUpperCase(), spaces));
            }

            println("");
        }

        // Print rows
        for (String[] aData : data) {
            for (int col = 0; col < aData.length; col++) {
                String content = aData[col];

                int contentLength = content == null ? 0 : content.length();

                int width = widths.get(col) != null ? widths.get(col) : contentLength;
                String spaces = StringUtils.repeat(" ", width - contentLength + spacing);
                print(String.format("%s%s", content, spaces));
            }

            println("");
        }
    }

    default void table(List<String> headers, List<List<String>> data, boolean printHeader) {
        String[][] dataArray = data
            .stream()
            .map(line -> line.toArray(new String[0]))
            .toArray(String[][]::new);

        String[] headersArray = headers.toArray(new String[0]);

        table(headersArray, dataArray, printHeader);
    }

    default void table(List<Pair<String, Object>> content) {
        String[][] contentStr = content.stream()
            .map(pair -> {
                String[] lineArr = new String[2];
                lineArr[0] = pair.getKey();
                lineArr[1] = pair.getValue() != null ? pair.getValue().toString() : "<null>";
                return lineArr;
            }).toArray(String[][]::new);

        table(new String[] {}, contentStr, false);
    }

    default void table(List<Pair<String, Object>> content, String keyHeader, String valueHeader) {
        String[][] contentStr = content.stream()
            .map(pair -> {
                String[] lineArr = new String[2];
                lineArr[0] = pair.getKey();
                lineArr[1] = pair.getValue() != null ? pair.getValue().toString() : "<null>";
                return lineArr;
            }).toArray(String[][]::new);

        table(new String[] { keyHeader, valueHeader }, contentStr, true);
    }

}
