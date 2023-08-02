package msifeed.mc.sys.utils;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {
    public static List<String> splitString(String input, int maxLineLength) {
        String[] words = input.split(" ");
        List<String> output = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (line.length() + word.length() > maxLineLength) {
                output.add(line.toString());
                line = new StringBuilder(word + " ");
                continue;
            }
            line.append(word).append(" ");
        }

        if (!line.toString().isEmpty()) {
            output.add(line.toString());
        }

        return output;
    }
}
