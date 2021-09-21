package revxrsal.commands.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.Flag;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Switch;

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static revxrsal.commands.util.Preconditions.checkArgument;
import static revxrsal.commands.util.Preconditions.notNull;

/**
 * General utilities for string operations
 */
public final class Strings {

    private Strings() {}

    /**
     * Pattern to split by whitespace
     */
    public static final Pattern SPACE = Pattern.compile(" ", Pattern.LITERAL);

    /**
     * Pattern to split by whitespace
     */
    @SuppressWarnings("RegExpEmptyAlternationBranch") // we have LITERAL :face_palm:
    public static final Pattern VERTICAL_BAR = Pattern.compile("|", Pattern.LITERAL);

    /**
     * Pattern to extract snowflake IDs. Useful for JDA
     */
    public static final Pattern SNOWFLAKE = Pattern.compile("<(@!|@|@&|#)(?<snowflake>\\d{18})>");

    public static LinkedList<String> splitBySpace(String text) {
        String[] result = SPACE.split(text);
        LinkedList<String> list = new LinkedList<>();
        Collections.addAll(list, result);
        return list;
    }

    public static @Nullable String getSnowflake(String mention) {
        Matcher matcher = SNOWFLAKE.matcher(mention);
        if (matcher.find())
            return matcher.group(2);
        return null;
    }

    public static String getName(@NotNull Parameter parameter) {
        Named named = parameter.getAnnotation(Named.class);
        if (named != null) {
            return named.value();
        }
        Switch switchAnn = parameter.getAnnotation(Switch.class);
        if (switchAnn != null) {
            return switchAnn.value();
        }
        Flag flag = parameter.getAnnotation(Flag.class);
        if (flag != null) {
            return flag.value();
        }
        return parameter.getName();
    }

    public static String repeat(String string, int count) {
        notNull(string, "string");

        if (count <= 1) {
            checkArgument(count >= 0, "invalid count: " + count);
            return (count == 0) ? "" : string;
        }

        final int len = string.length();
        final long longSize = (long) len * (long) count;
        final int size = (int) longSize;
        if (size != longSize) {
            throw new ArrayIndexOutOfBoundsException("Required array size too large: " + longSize);
        }

        final char[] array = new char[size];
        string.getChars(0, len, array, 0);
        int n;
        for (n = len; n < size - n; n <<= 1) {
            System.arraycopy(array, 0, array, n, n);
        }
        System.arraycopy(array, 0, array, n, size - n);
        return new String(array);
    }

    public static String colorize(@NotNull String text) {
        char[] b = text.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = '\u00A7';
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }
}
