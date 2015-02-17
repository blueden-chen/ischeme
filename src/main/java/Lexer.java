import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by chenwj on 2/11/15.
 */
public class Lexer {
    public static String[] tokenize(String text) {
        String[] tokens = text.replace("(", "( ").replace(")", " )").split("\\s+");
        return tokens;
    }

    public static String prettyPrint(String[] lexes) {
        return "[" + StringUtils.join(Arrays.asList(lexes).stream().map((x) -> "'" + x + "'").collect(Collectors.<String>toList()), ", ") + "]";
    }
}
